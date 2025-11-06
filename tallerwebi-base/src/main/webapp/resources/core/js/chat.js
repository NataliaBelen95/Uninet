// chat.js - ajuste: merge de historial, conservación de optimistas, logs más claros y re-subscribe
(function() {
    'use strict';

    const contextPath = document.body.getAttribute('data-context-path') || '';
    const myUserId = String(document.body.getAttribute('data-user-id') || '');

    let stompClient = null;
    let stompConnected = false;
    let subscription = null;
    let reconnectAttempts = 0;
    let reconnectTimer = null;

    const conversationsCache = {};
    let activeConversationId = null;

    function log(...args) { console.log('[chat]', ...args); }
    function warn(...args) { console.warn('[chat]', ...args); }
    function error(...args) { console.error('[chat]', ...args); }
    function $(id) { return document.getElementById(id); }

    function optimisticSignature(m) {
        return `${m.fromUserId}|${m.toUserId}|${m.content}|${m.timestamp || ''}`;
    }

    function addToCache(otherId, msg) {
        conversationsCache[otherId] = conversationsCache[otherId] || [];
        const list = conversationsCache[otherId];
        if (msg.id) {
            if (list.some(x => x.id && String(x.id) === String(msg.id))) return false;
            list.push(msg);
            return true;
        } else {
            const sig = optimisticSignature(msg);
            if (list.some(x => !x.id && optimisticSignature(x) === sig)) return false;
            list.push(msg);
            return true;
        }
    }

    function reconcileWithPersisted(otherId, persistedMsg) {
        const list = conversationsCache[otherId] || [];
        const sig = optimisticSignature(persistedMsg);
        const idx = list.findIndex(x => (!x.id) && optimisticSignature(x) === sig);
        if (idx !== -1) {
            list.splice(idx, 1, persistedMsg);
            conversationsCache[otherId] = list;
            return true;
        }
        return false;
    }

    // Merge server list with cache: keep optimists not present on server
    function mergeServerListWithCache(otherId, serverList) {
        const cache = conversationsCache[otherId] || [];
        const byId = new Map();
        (serverList || []).forEach(m => { if (m.id) byId.set(String(m.id), m); });

        const merged = [];
        const optimistSigs = new Set();

        (serverList || []).forEach(m => merged.push(m));

        merged.forEach(m => {
            if (!m.id) optimistSigs.add(optimisticSignature(m));
            else optimistSigs.add('id:' + String(m.id));
        });

        cache.forEach(m => {
            if (!m.id) {
                const sig = optimisticSignature(m);
                if (!optimistSigs.has(sig)) merged.push(m);
            } else {
                if (!byId.has(String(m.id))) merged.push(m);
            }
        });

        const seen = new Set();
        const finalList = [];
        merged.forEach(m => {
            const key = m.id ? 'id:' + String(m.id) : 'sig:' + optimisticSignature(m);
            if (!seen.has(key)) { finalList.push(m); seen.add(key); }
        });

        conversationsCache[otherId] = finalList;
    }

    function conectar() {
        if (stompConnected) return;
        tryConnectEndpoints(['/spring/ws', contextPath + '/ws']);
    }

    function tryConnectEndpoints(endpoints, i = 0) {
        if (i >= endpoints.length) { scheduleReconnect(); return; }
        const wsEndpoint = endpoints[i];
        log('Intentando conectar WS a:', wsEndpoint);
        try {
            const socket = new SockJS(wsEndpoint);
            const client = Stomp.over(socket);
            // habilitar debug temporal si necesitás ver frames (poner a console)
            client.debug = null;

            client.connect({}, function(frame) {
                log('STOMP conectado (endpoint):', wsEndpoint, frame);
                stompClient = client;
                stompConnected = true;
                reconnectAttempts = 0;
                if (subscription) {
                    try { subscription.unsubscribe(); } catch (e) {}
                    subscription = null;
                }
                ensureSubscription();
            }, function(err) {
                warn('Error conectando STOMP a', wsEndpoint, err);
                tryConnectEndpoints(endpoints, i + 1);
            });

            socket.onclose = function() {
                log('SockJS socket cerrado para endpoint', wsEndpoint);
                if (!stompConnected) tryConnectEndpoints(endpoints, i + 1);
                else {
                    stompConnected = false;
                    subscription = null;
                    scheduleReconnect();
                }
            };
        } catch (e) {
            warn('Error creando SockJS para', wsEndpoint, e);
            tryConnectEndpoints(endpoints, i + 1);
        }
    }

    function scheduleReconnect() {
        if (reconnectTimer) return;
        reconnectAttempts++;
        const wait = Math.min(30000, 1000 * Math.pow(1.5, reconnectAttempts));
        log('Reintento de conexión en ms:', wait);
        reconnectTimer = setTimeout(() => { reconnectTimer = null; conectar(); }, wait);
    }

    function ensureSubscription() {
        try {
            if (!stompClient || !stompConnected || !myUserId) {
                log('ensureSubscription: condiciones no cumplidas', { stompClient: !!stompClient, stompConnected, myUserId });
                return;
            }
            if (subscription) {
                log('Ya existe subscription para', myUserId);
                return;
            }
            const destino = '/topic/chat/' + myUserId;
            log('Suscribiendo a destino:', destino);
            subscription = stompClient.subscribe(destino, function(message) {
                try {
                    const msg = JSON.parse(message.body);
                    log('Mensaje WS recibido (stomp):', msg);
                    handleIncomingMessage(msg);
                } catch (e) {
                    error('Error parseando mensaje STOMP', e, message.body);
                }
            });
        } catch (e) {
            error('ensureSubscription error', e);
        }
    }

    function handleIncomingMessage(msg) {
        try {
            const otherId = String(msg.fromUserId) === String(myUserId) ? String(msg.toUserId) : String(msg.fromUserId);
            log('handleIncomingMessage -> otherId=', otherId, 'msgId=', msg.id || '(no id)');
            if (msg.id) {
                const reconciled = reconcileWithPersisted(otherId, msg);
                if (!reconciled) addToCache(otherId, msg);
            } else {
                addToCache(otherId, msg);
            }
            if (activeConversationId && String(activeConversationId) === String(otherId)) {
                renderConversacionFromArray(conversationsCache[otherId]);
            } else {
                markContactUnread(otherId);
            }
        } catch (e) {
            error('handleIncomingMessage error', e);
        }
    }

    function markContactUnread(otherId) {
        try { const a = document.querySelector(`.contacto[data-id="${otherId}"]`); if (a) a.classList.add('unread'); } catch (e) {}
    }

    function recibirMensaje(msg) {
        const conversacion = $('conversacion');
        if (!conversacion) return;
        const isMine = String(msg.fromUserId) === String(myUserId);
        const cont = document.createElement('div');
        cont.className = isMine ? 'mensaje-propio' : 'mensaje-otro';
        const dateObj = parseTimestamp(msg.timestamp);
        const time = !isNaN(dateObj) ? dateObj.toLocaleTimeString() : new Date().toLocaleTimeString();
        cont.innerHTML = `<div class="meta"><strong>${escapeHtml(msg.fromName || (isMine ? 'Yo' : 'Usuario'))}</strong> <span class="time">${time}</span></div>
                     <div class="texto">${escapeHtml(msg.content || '')}</div>`;
        if (msg.__optimistic) cont.classList.add('optimistic');
        conversacion.appendChild(cont);
        conversacion.scrollTop = conversacion.scrollHeight;
    }

    function escapeHtml(s) {
        return (s || '').toString().replace(/[&<>"']/g, m => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#039;' }[m]));
    }

    function parseTimestamp(ts) {
        if (!ts) return new Date();
        if (typeof ts === 'string') {
            const d = new Date(ts); if (!isNaN(d)) return d;
            const d2 = new Date(ts.replace(' ', 'T')); if (!isNaN(d2)) return d2;
        }
        if (typeof ts === 'number') return new Date(ts);
        if (typeof ts === 'object') {
            const y = ts.year || ts.y || ts.Y || 0;
            const mo = (ts.month || ts.monthValue || ts.m || 1) - 1;
            const da = ts.day || ts.d || ts.dayOfMonth || 1;
            const hh = ts.hour || ts.hourOfDay || 0;
            const mm = ts.minute || ts.min || 0;
            const ss = ts.second || ts.sec || 0;
            return new Date(y, mo, da, hh, mm, ss);
        }
        return new Date(String(ts));
    }

    async function enviarMensaje() {
        const btn = $('btnEnviar'); if (btn) btn.disabled = true;
        try {
            const toEl = $('toUserId'), txtEl = $('mensajeTexto');
            if (!toEl || !txtEl) { alert('Elementos de chat no encontrados'); return; }
            const toUserId = String(toEl.value || '').trim();
            const texto = String(txtEl.value || '').trim();
            if (!toUserId) { alert('Seleccioná un contacto'); return; }
            if (!texto) return;

            const payload = { fromUserId: parseInt(myUserId), toUserId: parseInt(toUserId), fromName: null, content: texto, timestamp: new Date().toISOString() };
            const optimistic = Object.assign({}, payload, { __optimistic: true });
            addToCache(toUserId, optimistic);
            if (activeConversationId === String(toUserId)) recibirMensaje(optimistic);

            if (stompClient && stompConnected) {
                try {
                    log('Enviando via STOMP', payload);
                    stompClient.send('/app/chat/enviar', { 'content-type': 'application/json' }, JSON.stringify(payload));
                } catch (e) {
                    warn('Error enviando por STOMP, fallback', e);
                    await fallbackEnviar(payload);
                }
            } else {
                log('STOMP no conectado, fallback HTTP');
                await fallbackEnviar(payload);
            }
            txtEl.value = '';
        } catch (e) {
            error('Error en enviarMensaje', e);
            alert('No se pudo enviar el mensaje. Revisa la consola.');
        } finally {
            if (btn) btn.disabled = false;
        }
    }

    async function fallbackEnviar(payload) {
        const url = contextPath + '/chat/test/enviar';
        const r = await fetch(url, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload) });
        if (!r.ok) throw new Error('fallback status ' + r.status);
        log('Fallback OK', r.status);
    }

    async function cargarConversacion(otherId, forceNetwork = false) {
        if (!otherId) return;
        if (!forceNetwork && conversationsCache[otherId]) { renderConversacionFromArray(conversationsCache[otherId]); return; }
        const url = contextPath + '/chat/conversacion?withUser=' + encodeURIComponent(otherId);
        log('Fetching conversacion', url);
        try {
            const r = await fetch(url);
            if (r.status === 302 || r.redirected) { warn('Fetch redirected (posible sesión expirada).', r); return; }
            if (!r.ok) {
                error('Error al cargar conversacion', r.status);
                const conv = $('conversacion');
                if (conv) {
                    const aviso = document.createElement('div');
                    aviso.className = 'aviso-error';
                    aviso.textContent = 'Error al cargar la conversación (server ' + r.status + ').';
                    conv.insertAdjacentElement('afterbegin', aviso);
                    setTimeout(() => aviso.remove(), 3000);
                }
                return;
            }
            const data = await r.json();
            mergeServerListWithCache(otherId, data || []);
            renderConversacionFromArray(conversationsCache[otherId]);
        } catch (e) {
            error('cargarConversacion error', e);
            const conv = $('conversacion');
            if (conv) {
                const aviso = document.createElement('div');
                aviso.className = 'aviso-error';
                aviso.textContent = 'Error de red al cargar la conversación.';
                conv.insertAdjacentElement('afterbegin', aviso);
                setTimeout(() => aviso.remove(), 3000);
            }
        }
    }

    function renderConversacionFromArray(arr) {
        const conv = $('conversacion');
        if (!conv) return;
        conv.innerHTML = '';
        const sorted = (arr || []).slice().sort((a, b) => parseTimestamp(a.timestamp).getTime() - parseTimestamp(b.timestamp).getTime());
        sorted.forEach(m => recibirMensaje(m));
    }

    function seleccionarContacto(element) {
        if (!element) return;
        const id = String(element.getAttribute('data-id'));
        const nombre = (element.querySelector('span') && element.querySelector('span').textContent) || element.textContent || 'Contacto';
        log('Contacto seleccionado', id, nombre);
        $('toUserId').value = id;
        $('nombreContacto').textContent = nombre;

        document.querySelectorAll('.contacto-item').forEach(li => {
            try {
                const a = li.querySelector('.contacto');
                if (!a) return;
                if (String(a.getAttribute('data-id')) === id) li.classList.remove('minimized');
                else li.classList.add('minimized');
            } catch (e) {}
        });

        activeConversationId = id;

        if (!stompConnected) conectar();
        else ensureSubscription();

        if (conversationsCache[id]) renderConversacionFromArray(conversationsCache[id]);
        cargarConversacion(id, true);

        const a = document.querySelector(`.contacto[data-id="${id}"]`);
        if (a) a.classList.remove('unread');
    }

    document.addEventListener('DOMContentLoaded', function() {
        try {
            if (!myUserId) warn('myUserId no definido en body');
            const btn = $('btnEnviar');
            if (btn) {
                btn.disabled = false;
                btn.removeEventListener('click', enviarMensaje);
                btn.addEventListener('click', enviarMensaje);
            }

            const lista = $('lista-contactos');
            if (lista) {
                lista.addEventListener('click', function(evt) {
                    const a = evt.target.closest('.contacto');
                    if (a) {
                        evt.preventDefault();
                        seleccionarContacto(a);
                    }
                });
            }

            conectar();
            const initial = ($('toUserId') && $('toUserId').value) || null;
            if (initial) {
                activeConversationId = String(initial);
                cargarConversacion(initial, true);
            }
        } catch (e) {
            error('init error', e);
        }
    });

    window.addEventListener('beforeunload', function() {
        try {
            if (subscription) {
                try {
                    subscription.unsubscribe();
                } catch (e) {}
                subscription = null;
            }
            if (stompClient && stompConnected) {
                try {
                    stompClient.disconnect();
                } catch (e) {}
            }
        } catch (e) {}
    });

    // export para debugging
    window._chatDebug = {
        conectar,
        ensureSubscription,
        enviarMensaje,
        cargarConversacion,
        conversationsCache,
        activeConversationId
    };

})();
// chat.js - versión robusta para STOMP + fallback REST
(function() {
    'use strict';

    // DOM attributes
    const contextPath = document.body.getAttribute('data-context-path') || '';
    const myUserId = document.body.getAttribute('data-user-id') || null;

    let stompClient = null;
    let stompConnected = false;
    let subscription = null;
    const conversationsCache = {};

    function log(...args) { console.log('[chat]', ...args); }
    function warn(...args) { console.warn('[chat]', ...args); }
    function error(...args) { console.error('[chat]', ...args); }

    function $(id) { return document.getElementById(id); }

    // Conectar al endpoint SockJS usando el contextPath inyectado
    function conectar() {
        try {
            const wsEndpoint = contextPath + '/ws';
            log('Conectando WS a:', wsEndpoint);
            const socket = new SockJS(wsEndpoint);
            stompClient = Stomp.over(socket);
            stompClient.debug = null;
            stompClient.connect({}, function(frame) {
                log('STOMP conectado:', frame);
                stompConnected = true;
                ensureSubscription();
            }, function(err) {
                stompConnected = false;
                error('STOMP connect error:', err);
            });
        } catch (e) {
            error('Error en conectar():', e);
            stompConnected = false;
        }
    }

    function ensureSubscription() {
        try {
            if (!stompClient || !stompConnected || !myUserId) {
                log('ensureSubscription: condiciones no cumplidas', {stompClient: !!stompClient, stompConnected, myUserId});
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
                    log('Mensaje WS recibido:', msg);
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
            conversationsCache[otherId] = conversationsCache[otherId] || [];
            conversationsCache[otherId].push(msg);

            const current = ($('toUserId') && $('toUserId').value) ? String($('toUserId').value) : null;
            if (current === otherId) {
                recibirMensaje(msg);
            } else {
                markContactUnread(otherId);
            }
        } catch (e) {
            error('handleIncomingMessage error', e);
        }
    }

    function markContactUnread(otherId) {
        try {
            const a = document.querySelector(`.contacto[data-id="${otherId}"]`);
            if (a) a.classList.add('unread');
        } catch (e) {}
    }

    function recibirMensaje(msg) {
        const conversacion = $('conversacion');
        if (!conversacion) return;
        const isMine = String(msg.fromUserId) === String(myUserId);
        const cont = document.createElement('div');
        cont.className = isMine ? 'mensaje-propio' : 'mensaje-otro';
        const time = msg.timestamp ? new Date(msg.timestamp).toLocaleTimeString() : new Date().toLocaleTimeString();
        const safeText = (msg.content || '').replace(/[&<>"']/g, m => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#039;'}[m]));
        cont.innerHTML = `<div class="meta"><strong>${escapeHtml(msg.fromName || (isMine ? 'Yo' : 'Usuario'))}</strong> <span class="time">${time}</span></div>
                      <div class="texto">${safeText}</div>`;
        conversacion.appendChild(cont);
        conversacion.scrollTop = conversacion.scrollHeight;
    }

    function escapeHtml(s) { return (s || '').toString().replace(/[&<>"']/g, m => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#039;'}[m])); }

    async function enviarMensaje() {
        const btn = $('btnEnviar'); if (btn) btn.disabled = true;
        try {
            const toEl = $('toUserId'), txtEl = $('mensajeTexto');
            if (!toEl || !txtEl) { alert('Elementos de chat no encontrados'); return; }
            const toUserId = String(toEl.value || '').trim();
            const texto = String(txtEl.value || '').trim();
            if (!toUserId) { alert('Seleccioná un contacto'); return; }
            if (!texto) { return; }

            const payload = {
                fromUserId: parseInt(myUserId),
                toUserId: parseInt(toUserId),
                fromName: null,
                content: texto,
                timestamp: new Date().toISOString()
            };

            // Optimistic UI + cache
            conversationsCache[toUserId] = conversationsCache[toUserId] || [];
            conversationsCache[toUserId].push({...payload, fromName: 'Yo'});
            recibirMensaje({...payload, fromName: 'Yo'});

            if (stompClient && stompConnected) {
                try {
                    log('Enviando via STOMP', payload);
                    // IMPORTANT: enviar header content-type para que Spring lo convierta correctamente
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
            // give server a short time to persist (if necessary) then refresh persisted conversation
            await new Promise(r => setTimeout(r, 150));
            await cargarConversacion(toUserId, true);
        } catch (e) {
            error('Error en enviarMensaje', e);
            alert('No se pudo enviar el mensaje. Revisa la consola.');
        } finally {
            if (btn) btn.disabled = false;
        }
    }

    async function fallbackEnviar(payload) {
        const url = contextPath + '/chat/test/enviar';
        const r = await fetch(url, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(payload)
        });
        if (!r.ok) throw new Error('fallback status ' + r.status);
        log('Fallback OK', r.status);
    }

    async function cargarConversacion(otherId, forceNetwork = false) {
        if (!otherId) return;
        if (!forceNetwork && conversationsCache[otherId]) {
            renderConversacionFromArray(conversationsCache[otherId]);
            return;
        }
        const url = contextPath + '/chat/conversacion?withUser=' + encodeURIComponent(otherId);
        log('Fetching conversacion', url);
        const r = await fetch(url);
        if (r.status === 302 || r.redirected) {
            warn('Fetch redirected (posible sesión expirada).', r);
            return;
        }
        if (!r.ok) {
            error('Error al cargar conversacion', r.status);
            return;
        }
        const data = await r.json();
        conversationsCache[otherId] = data || [];
        renderConversacionFromArray(conversationsCache[otherId]);
    }

    function renderConversacionFromArray(arr) {
        const conv = $('conversacion'); if (!conv) return;
        conv.innerHTML = '';
        (arr || []).forEach(m => recibirMensaje(m));
    }

    function seleccionarContacto(element) {
        if (!element) return;
        const id = element.getAttribute('data-id');
        const nombre = (element.querySelector('span') && element.querySelector('span').textContent) || element.textContent || 'Contacto';
        log('Contacto seleccionado', id, nombre);
        $('toUserId').value = id;
        $('nombreContacto').textContent = nombre;

        document.querySelectorAll('.contacto-item').forEach(li => {
            try {
                const a = li.querySelector('.contacto');
                if (!a) return;
                if (a.getAttribute('data-id') === String(id)) li.classList.remove('minimized');
                else li.classList.add('minimized');
            } catch (e) {}
        });

        if (!stompConnected) conectar();
        else ensureSubscription();

        if (conversationsCache[id]) renderConversacionFromArray(conversationsCache[id]);
        cargarConversacion(id, true);
    }

    document.addEventListener('DOMContentLoaded', function() {
        try {
            if (!$('btnEnviar')) warn('#btnEnviar no encontrado en DOM');
            if (!$('lista-contactos')) warn('#lista-contactos no encontrado en DOM');
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
                    if (a) { evt.preventDefault(); seleccionarContacto(a); }
                });
            }

            conectar();
            const initial = ($('toUserId') && $('toUserId').value) || null;
            if (initial) cargarConversacion(initial, true);
        } catch (e) {
            error('init error', e);
        }
    });
    // helper: convierte distintos formatos a Date
    function parseTimestamp(ts) {
        if (!ts) return new Date();
        // si ya es string
        if (typeof ts === 'string') {
            const d = new Date(ts);
            if (!isNaN(d)) return d;
            // intentar con reemplazo (safari IE issues)
            const alt = ts.replace(' ', 'T');
            const d2 = new Date(alt);
            if (!isNaN(d2)) return d2;
        }
        // si es número (epoch millis)
        if (typeof ts === 'number') {
            return new Date(ts);
        }
        // si es objeto tipo { year, month, day, hour, minute, second }
        if (typeof ts === 'object') {
            const y = ts.year || ts.y || ts.Y || 0;
            const mo = (ts.month || ts.monthValue || ts.m || 1) - 1; // months 0-based
            const da = ts.day || ts.d || ts.dayOfMonth || 1;
            const hh = ts.hour || ts.hourOfDay || 0;
            const mm = ts.minute || ts.min || 0;
            const ss = ts.second || ts.sec || 0;
            return new Date(y, mo, da, hh, mm, ss);
        }
        // fallback
        return new Date(String(ts));
    }

    const dateObj = parseTimestamp(msg.timestamp);
    const time = !isNaN(dateObj) ? dateObj.toLocaleTimeString() : new Date().toLocaleTimeString();

    // export para debugging
    window._chatDebug = { conectar, ensureSubscription, enviarMensaje, cargarConversacion, conversationsCache };

})();
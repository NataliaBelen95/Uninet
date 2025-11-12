// Reemplaza tu notificaciones.js por este.
// Resumen: el dropdown usa /spring/notificaciones-dropdown (sin cambios).
// Si un item es SOLICITUD_AMISTAD el JSON debe exponer usuarioEmisorId y usuarioReceptorId.
// Los botones Aceptar/Rechazar llamarán a /spring/amistad/aceptar-por-ids y /spring/amistad/rechazar-por-ids.
document.addEventListener("DOMContentLoaded", function() {
    console.log("JS de notificaciones cargado");

    const btnNotificaciones = document.getElementById('btn-notificaciones');
    const dropdown = document.getElementById('dropdown-notificaciones');
    const lista = document.getElementById('listaNotificaciones');

    if (!btnNotificaciones) return;

    const userId = btnNotificaciones.dataset.userId || 0;

    // CSRF headers (lee las meta tags si existen)
    const metaCsrfToken = document.querySelector('meta[name="_csrf"]');
    const metaCsrfHeader = document.querySelector('meta[name="_csrf_header"]');
    const CSRF_TOKEN = metaCsrfToken ? metaCsrfToken.getAttribute('content') : null;
    const CSRF_HEADER = metaCsrfHeader ? metaCsrfHeader.getAttribute('content') : null;
    function getCsrfHeaders() {
        if (CSRF_TOKEN && CSRF_HEADER) {
            const h = {};
            h[CSRF_HEADER] = CSRF_TOKEN;
            return h;
        }
        return {};
    }

    // WebSocket (igual que antes)
    try {
        const socket = new SockJS('/spring/ws');
        const stompClient = Stomp.over(socket);
        stompClient.connect({}, function() {
            console.log("🟢 Conectado al WebSocket");
            stompClient.subscribe('/topic/notificaciones-' + userId, function(message) {
                const cantidad = parseInt(message.body);
                actualizarBadge(cantidad);
                cargarNotificaciones();
            });
        });
    } catch (e) {
        console.warn('WS no disponible', e);
    }

    // Mostrar/ocultar dropdown
    btnNotificaciones.addEventListener('click', function(e) {
        e.preventDefault();
        if (!dropdown) return;
        const visible = window.getComputedStyle(dropdown).display !== 'none';
        dropdown.style.display = visible ? 'none' : 'block';
        if (!visible) cargarNotificaciones();
    });

    function actualizarBadge(cantidad) {
        let badge = btnNotificaciones.querySelector('.badge');
        if (cantidad > 0) {
            if (!badge) {
                badge = document.createElement('span');
                badge.classList.add('badge');
                btnNotificaciones.appendChild(badge);
            }
            badge.textContent = cantidad;
        } else if (badge) {
            badge.remove();
        }
    }

    function escapeHtml(s) {
        return String(s || '').replace(/[&<>"']/g, function(m) { return ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#039;'}[m]); });
    }

    // Construir <li> desde cada objeto de DatosNotificacion (JSON)
    function buildLiForNotification(n) {
        const li = document.createElement('li');
        if (n.leida) li.classList.add('leida');
        if (n.id) li.setAttribute('data-notificacion-id', n.id);
        if (n.url) li.setAttribute('data-url', n.url);

        // Exponer ids de usuarios en el DOM (para uso por los botones)
        if (n.usuarioEmisorId) li.setAttribute('data-usuario-emisor-id', n.usuarioEmisorId);
        if (n.usuarioReceptorId) li.setAttribute('data-usuario-receptor-id', n.usuarioReceptorId);

        const emisor = n.usuarioEmisor || 'Sistema';
        // fecha
        let fechaForm = '';
        if (n.fecha) {
            const s = String(n.fecha).split('.')[0];
            const d = new Date(s);
            if (!isNaN(d)) fechaForm = d.getFullYear() + '.' + String(d.getMonth()+1).padStart(2,'0') + '.' + String(d.getDate()).padStart(2,'0');
        }

        // si es solicitud (reconocemos por el texto o por la existencia de emisorId)
        const isSolicitud = /solicitud/i.test(String(n.mensaje)) || n.usuarioEmisorId;

        if (isSolicitud) {
            const div = document.createElement('div');
            div.className = 'noti-contenido';
            div.innerHTML = `<strong>${escapeHtml(emisor)}</strong>: ${escapeHtml(n.mensaje || '')} <span class="fecha">${fechaForm}</span>`;
            li.appendChild(div);

            const acciones = document.createElement('div');
            acciones.className = 'acciones-solicitud';

            const btnAceptar = document.createElement('button');
            btnAceptar.type = 'button';
            btnAceptar.className = 'btn-aceptar';
            btnAceptar.textContent = 'Aceptar';
            acciones.appendChild(btnAceptar);

            const btnRechazar = document.createElement('button');
            btnRechazar.type = 'button';
            btnRechazar.className = 'btn-rechazar';
            btnRechazar.textContent = 'Rechazar';
            acciones.appendChild(btnRechazar);

            // link "Ver" opcional (va a la URL de la notificación)
            if (n.url) {
                const ver = document.createElement('a');
                ver.href = n.url.startsWith('/spring') ? n.url : '/spring' + n.url;
                ver.className = 'ver-notificacion';
                ver.textContent = 'Ver';
                acciones.appendChild(ver);
            }

            li.appendChild(acciones);

            // importante: evitar que el click sobre botones dispare el handler del li (ver más abajo)
        } else {
            li.innerHTML = `<strong>${escapeHtml(emisor)}</strong>: ${escapeHtml(n.mensaje || '')} <span class="fecha">${fechaForm}</span>`;
            // clic en li = marcar leída y redirigir (si corresponde)
            li.addEventListener('click', function(evt) {
                // si clic vino de un botón dentro del li, ignoramos
                if (evt.target.closest('button') || evt.target.closest('a')) return;

                if (!n.id) return;
                fetch(`/spring/marcar-leida/${n.id}`, { method: 'POST', headers: getCsrfHeaders() })
                    .then(res => {
                        if (!res.ok) throw new Error('Error al marcar como leída');
                        // actualizar badge
                        const badge = btnNotificaciones.querySelector('.badge');
                        if (badge) {
                            let c = parseInt(badge.textContent || '0');
                            c = Math.max(0, c - 1);
                            if (c === 0) badge.remove();
                            else badge.textContent = c;
                        }
                        if (n.url) {
                            const urlFinal = n.url.startsWith('/spring') ? n.url : '/spring' + n.url;
                            if (window.location.pathname !== urlFinal) window.location.href = urlFinal;
                        }
                    })
                    .catch(err => console.error(err));
            });
        }

        return li;
    }

    async function cargarNotificaciones() {
        try {
            const resp = await fetch('/spring/notificaciones-dropdown');
            if (!resp.ok) throw new Error('Error cargando notificaciones: ' + resp.status);
            const data = await resp.json();
            lista.innerHTML = '';
            if (!Array.isArray(data) || data.length === 0) {
                const li = document.createElement('li');
                li.textContent = 'No hay notificaciones';
                li.style.fontStyle = 'italic';
                li.style.color = '#666';
                lista.appendChild(li);
                actualizarBadge(0);
                return;
            }
            data.forEach(n => {
                const li = buildLiForNotification(n);
                lista.appendChild(li);
            });
            const noLeidas = data.filter(x => !x.leida).length;
            actualizarBadge(noLeidas);
        } catch (err) {
            console.error('Error en cargarNotificaciones:', err);
            lista.innerHTML = '<li style="color:#666;font-style:italic">Error al cargar notificaciones</li>';
            actualizarBadge(0);
        }
    }

    // Delegación: botones Aceptar/Rechazar (usa emisor/receptor ids si están disponibles)
    document.body.addEventListener('click', function(evt) {
        const aceptarBtn = evt.target.closest('.btn-aceptar');
        const rechazarBtn = evt.target.closest('.btn-rechazar');

        if (aceptarBtn) {
            evt.preventDefault();
            // evitar que el li capture el click (aunque ya protegimos el li, redundancia)
            evt.stopPropagation();

            // primero intentar leer ids desde el li
            const li = aceptarBtn.closest('li');
            const emisorId = li?.getAttribute('data-usuario-emisor-id') || null;
            const receptorId = li?.getAttribute('data-usuario-receptor-id') || null;
            const notiId = li?.getAttribute('data-notificacion-id') || null;

            if (emisorId && receptorId) {
                aceptarPorIdsAjax(Number(emisorId), Number(receptorId), notiId, aceptarBtn);
            } else {
                alert('No se pudo identificar la solicitud. Abrí la página de notificaciones y probá allí.');
            }
            return;
        }

        if (rechazarBtn) {
            evt.preventDefault();
            evt.stopPropagation();
            const li = rechazarBtn.closest('li');
            const emisorId = li?.getAttribute('data-usuario-emisor-id') || null;
            const receptorId = li?.getAttribute('data-usuario-receptor-id') || null;
            const notiId = li?.getAttribute('data-notificacion-id') || null;

            if (emisorId && receptorId) {
                rechazarPorIdsAjax(Number(emisorId), Number(receptorId), notiId, rechazarBtn);
            } else {
                alert('No se pudo identificar la solicitud. Abrí la página de notificaciones y probá allí.');
            }
            return;
        }
    });

    async function aceptarPorIdsAjax(solicitanteId, receptorId, notiId, btn) {
        const url = '/spring/amistad/aceptar-por-ids';
        const headers = Object.assign({'Content-Type':'application/json'}, getCsrfHeaders());
        try {
            const resp = await fetch(url, {
                method: 'POST',
                headers: headers,
                body: JSON.stringify({ solicitanteId: solicitanteId, receptorId: receptorId })
            });
            if (!resp.ok) throw new Error('Error al aceptar la solicitud');
            if (notiId) await fetch(`/spring/marcar-leida/${notiId}`, { method:'POST', headers: getCsrfHeaders() }).catch(()=>{});
            const row = btn.closest('li');
            if (row) {
                row.classList.add('solicitud-resuelta');
                row.querySelectorAll('.acciones-solicitud').forEach(n => n.remove());
                const estado = document.createElement('span'); estado.className='estado-solicitud'; estado.textContent='Aceptada';
                row.appendChild(estado);
            } else window.location.reload();
            // badge update
            const badge = btnNotificaciones.querySelector('.badge');
            if (badge) {
                let c = parseInt(badge.textContent || '0');
                c = Math.max(0, c - 1);
                if (c === 0) badge.remove();
                else badge.textContent = c;
            }
        } catch (e) {
            console.error(e);
            alert('No se pudo aceptar la solicitud por ids.');
        }
    }

    async function rechazarPorIdsAjax(solicitanteId, receptorId, notiId, btn) {
        const url = '/spring/amistad/rechazar-por-ids';
        const headers = Object.assign({'Content-Type':'application/json'}, getCsrfHeaders());
        try {
            const resp = await fetch(url, {
                method: 'POST',
                headers: headers,
                body: JSON.stringify({ solicitanteId: solicitanteId, receptorId: receptorId })
            });
            if (!resp.ok) throw new Error('Error al rechazar la solicitud');
            if (notiId) await fetch(`/spring/marcar-leida/${notiId}`, { method:'POST', headers: getCsrfHeaders() }).catch(()=>{});
            const row = btn.closest('li');
            if (row) {
                row.classList.add('solicitud-resuelta');
                row.querySelectorAll('.acciones-solicitud').forEach(n => n.remove());
                const estado = document.createElement('span'); estado.className='estado-solicitud'; estado.textContent='Rechazada';
                row.appendChild(estado);
            } else window.location.reload();
            const badge = btnNotificaciones.querySelector('.badge');
            if (badge) {
                let c = parseInt(badge.textContent || '0');
                c = Math.max(0, c - 1);
                if (c === 0) badge.remove();
                else badge.textContent = c;
            }
        } catch (e) {
            console.error(e);
            alert('No se pudo rechazar la solicitud por ids.');
        }
    }

});
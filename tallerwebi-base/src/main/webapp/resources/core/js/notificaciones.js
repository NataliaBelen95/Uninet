document.addEventListener("DOMContentLoaded", function() {
    console.log("JS de notificaciones cargado");

    const btnNotificaciones = document.getElementById('btn-notificaciones');
    const dropdown = document.getElementById('dropdown-notificaciones');
    const lista = document.getElementById('listaNotificaciones');

    // 🛑 CORRECCIÓN DE DESPLIEGUE: Si falta cualquier elemento, salimos.
    if (!btnNotificaciones || !dropdown || !lista) {
        console.error("Faltan elementos HTML del dropdown (btn-notificaciones, dropdown-notificaciones, o listaNotificaciones).");
        return;
    }

    const userId = btnNotificaciones.dataset.userId || 0;

    // 🔌 Conexión WebSocket (Sin cambios)
    const socket = new SockJS('/spring/ws');
    const stompClient = Stomp.over(socket);

    stompClient.connect({}, function() {
        console.log("🟢 Conectado al WebSocket");

        stompClient.subscribe('/topic/notificaciones-' + userId, function(message) {
            console.log("Mensaje recibido en WebSocket:", message.body);
            const cantidad = parseInt(message.body);
            actualizarBadge(cantidad);
            cargarNotificaciones();
        });
    });

    // Mostrar/ocultar dropdown
    btnNotificaciones.addEventListener('click', function(e) {
        e.preventDefault();
        const isHidden = dropdown.style.display === 'none' || dropdown.style.display === '';

        if (isHidden) {
            dropdown.style.display = 'block';
            cargarNotificaciones();
        } else {
            dropdown.style.display = 'none';
        }
    });

    // Actualiza badge
    function actualizarBadge(cantidad) {
        let badge = btnNotificaciones.querySelector('.badge');
        if (cantidad > 0) {
            if (!badge) {
                badge = document.createElement('span');
                badge.classList.add('badge');
                btnNotificaciones.appendChild(badge);
            }
            badge.textContent = cantidad;
            badge.style.display = 'inline-block';
        } else if (badge) {
            badge.remove();
        }
    }

    // Carga notificaciones en el dropdown
    function cargarNotificaciones() { // <-- INICIO DE LA FUNCIÓN
        fetch('/spring/notificaciones-dropdown')
            .then(res => res.json())
            .then(data => {
                lista.innerHTML = '';

                // 🛑 FILTRO DE INACTIVIDAD: Excluir por Tipo de Notificación
                const datosFiltrados = data.filter(n =>
                    n.tipo !== "INACTIVIDAD"
                );

                if (!datosFiltrados || datosFiltrados.length === 0) {
                    const li = document.createElement('li');
                    li.textContent = 'No hay notificaciones';
                    li.style.fontStyle = 'italic';
                    li.style.color = '#666';
                    lista.appendChild(li);
                    actualizarBadge(0);
                } else {
                    datosFiltrados.forEach(n => {
                        const li = document.createElement('li');

                        const esSolicitudAmistad = n.tipo === "SOLICITUD_AMISTAD" || n.amistadId != null;
                        const emisor = n.usuarioEmisor || 'Sistema';
                        const fechaStr = n.fecha ? n.fecha.split('.')[0] : '';
                        const fechaObj = new Date(fechaStr);
                        const fechaFormateada = fechaObj.getFullYear() + '.' +
                            String(fechaObj.getMonth()+1).padStart(2,'0') + '.' +
                            String(fechaObj.getDate()).padStart(2,'0');

                        li.innerHTML = `<strong>${emisor}</strong>: ${n.mensaje} <span class="fecha">${fechaFormateada}</span>`;

                        if (n.leida) li.classList.add('leida');

                        // --- INICIO DE LÓGICA DE EVENTOS ---
                        if (esSolicitudAmistad) {
                            const btnAceptar = document.createElement('button');
                            btnAceptar.textContent = 'Aceptar';
                            btnAceptar.classList.add('btn-aceptar-dropdown');
                            li.appendChild(btnAceptar);

                            // Evento Clic del botón ACEPTAR (AJAX)
                            btnAceptar.addEventListener('click', (e) => {
                                e.stopPropagation();

                                const notificacionId = n.id;
                                const solicitudId = n.amistadId;

                                if (!solicitudId) {
                                    console.error("Falta el ID de la Solicitud para aceptar.");
                                    alert("Error: No se encontró el ID de la solicitud.");
                                    return;
                                }

                                fetch(`/spring/amistad/aceptar/${solicitudId}`, {
                                    method: 'POST'
                                })
                                .then(res => {
                                    if (!res.ok) {
                                        return res.text().then(text => { throw new Error('Error al aceptar: ' + text); });
                                    }

                                    // Si la aceptación fue 200 OK, marcamos como leída
                                    return fetch(`/spring/marcar-leida/${notificacionId}`, { method: 'POST' });
                                })
                                .then(res => {
                                    if (!res.ok) throw new Error('Error al marcar como leída.');

                                    // Éxito: Eliminar LI y recargar
                                    li.remove();
                                    cargarNotificaciones();
                                })
                                .catch(err => {
                                    console.error("Fallo en la acción de aceptar:", err);
                                    alert("Hubo un error al procesar la solicitud: " + err.message);
                                });
                            });

                            // Evento Clic del LI (Redirigir a Solicitudes)
                            li.addEventListener('click', (e) => {
                                // Evitar redirección si se hizo clic en el botón.
                                if (e.target.classList.contains('btn-aceptar-dropdown')) {
                                    return;
                                }

                                // Marcar como leída y luego redirigir a la pestaña de solicitudes
                                fetch(`/spring/marcar-leida/${n.id}`, { method: 'POST' })
                                    .then(() => {
                                        const urlFinal = n.url.startsWith('/spring') ? n.url : '/spring' + n.url;
                                        window.location.href = urlFinal;
                                    })
                                    .catch(err => console.error(err));
                            });


                        } else {
                            // 🔗 CASO: General
                            li.addEventListener('click', () => {
                                fetch(`/spring/marcar-leida/${n.id}`, { method: 'POST' })
                                    .then(res => {
                                        if (!res.ok) throw new Error('Error al marcar como leída');

                                        // Actualizar badge
                                        const badge = btnNotificaciones.querySelector('.badge');
                                        if (badge) {
                                            let c = parseInt(badge.textContent);
                                            c = Math.max(0, c - 1);
                                            if (c === 0) badge.remove();
                                            else badge.textContent = c;
                                        }

                                        // Redirigir
                                        const urlFinal = n.url.startsWith('/spring') ? n.url : '/spring' + n.url;
                                        if (window.location.pathname !== urlFinal) {
                                            window.location.href = urlFinal;
                                        }
                                    })
                                    .catch(err => console.error(err));
                            });
                        }
                        // --- FIN DE LÓGICA DE EVENTOS ---

                        lista.appendChild(li);
                    });

                    actualizarBadge(datosFiltrados.length);
                }
            })
            .catch(err => console.error("Fallo en la carga del dropdown:", err));
        } // <-- CIERRE DE LA FUNCIÓN cargarNotificaciones()
});
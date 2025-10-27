document.addEventListener("DOMContentLoaded", function() {
    console.log("JS de notificaciones cargado");

    const btnNotificaciones = document.getElementById('btn-notificaciones');
    const dropdown = document.getElementById('dropdown-notificaciones');
    const lista = document.getElementById('listaNotificaciones');

    if (!btnNotificaciones) return;

    const userId = btnNotificaciones.dataset.userId || 0;

    // 🔌 Conexión WebSocket
    const socket = new SockJS('/spring/ws');
    const stompClient = Stomp.over(socket);

    stompClient.connect({}, function() {
        console.log("🟢 Conectado al WebSocket");

        // 🔔 Escuchar notificaciones en tiempo real
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
        dropdown.style.display = dropdown.style.display === 'none' ? 'block' : 'none';
        if (dropdown.style.display === 'block') cargarNotificaciones();
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
    function cargarNotificaciones() {
        fetch('/spring/notificaciones-dropdown')
            .then(res => res.json())
            .then(data => {
                lista.innerHTML = '';

                if (data.length === 0) {
                    const li = document.createElement('li');
                    li.textContent = 'No hay notificaciones';
                    li.style.fontStyle = 'italic';
                    li.style.color = '#666';
                    lista.appendChild(li);
                    actualizarBadge(0); // 🔹 aseguramos que el badge desaparezca
                } else {
                    data.forEach(n => {
                        const li = document.createElement('li');

                        // Formatear fecha
                        const fechaObj = new Date(n.fecha);
                        const fechaFormateada = `${fechaObj.getFullYear()}.${(fechaObj.getMonth()+1).toString().padStart(2,'0')}.${fechaObj.getDate().toString().padStart(2,'0')}`;

                        li.innerHTML = `<strong>${n.usuarioEmisor}</strong>: ${n.mensaje} <span class="fecha">${fechaFormateada}</span>`;
                        if (n.leida) li.classList.add('leida');

                        // 🔹 Evento clic
                        li.addEventListener('click', () => {
                            fetch(`/spring/marcar-leida/${n.id}`, { method: 'POST' })
                                .then(res => {
                                    if (!res.ok) throw new Error('Error al marcar como leída');

                                    // actualizar badge con lo que viene del WS o local
                                    const badge = btnNotificaciones.querySelector('.badge');
                                    if (badge) {
                                        let c = parseInt(badge.textContent);
                                        c = Math.max(0, c - 1);
                                        if (c === 0) badge.remove();
                                        else badge.textContent = c;
                                    }

                                    // redirigir solo si existe URL
                                    if (n.url && n.url.trim() !== '') {
                                        // 🔹 No duplicar /spring
                                        window.location.href = n.url.startsWith('/spring') ? n.url : '/spring' + n.url;
                                    } else {
                                        console.warn("Notificación sin URL definida:", n);
                                    }
                                })
                                .catch(err => console.error(err));
                        });

                        lista.appendChild(li);
                    });

                    // 🔹 Actualizamos badge con cantidad real
                    actualizarBadge(data.length);
                }
            })
            .catch(err => console.error(err));
    }
});

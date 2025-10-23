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
          console.log("Cantidad parseada:", cantidad);

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

    // 🧭 Actualiza badge siempre desde servidor (no manualmente)
 function actualizarBadge(cantidad) {
     let badge = btnNotificaciones.querySelector('.badge');
     if (cantidad > 0) {
         if (!badge) {
             console.log("Creando badge porque cantidad pasó de 0 a", cantidad);
             badge = document.createElement('span');
             badge.classList.add('badge');
             btnNotificaciones.appendChild(badge);
         }
         badge.textContent = cantidad;
         badge.style.display = 'inline-block';
     } else if (badge) {
         console.log("Removiendo badge porque cantidad es 0");
         badge.remove();  // Eliminar completamente el badge
     }
 }

    // Carga notificaciones en el menú
    function cargarNotificaciones() {
        fetch('/spring/notificaciones-dropdown')
            .then(res => res.json())
            .then(data => {
                lista.innerHTML = '';

                if (data.length === 0) {
                    const li = document.createElement('li');
                    li.textContent = 'No hay notificaciones';
                    li.style.fontStyle = 'italic';
                    li.style.color = '#666'; // opcional para que se vea más suave
                    lista.appendChild(li);
                } else {
                   data.forEach(n => {
                       const li = document.createElement('li');  // <--- Aquí creas el li

                       const f = n.fecha;
                       const fechaObj = new Date(f[0], f[1] - 1, f[2], f[3], f[4], f[5]);
                       const anio = fechaObj.getFullYear();
                       const mes = (fechaObj.getMonth() + 1).toString().padStart(2, '0');
                       const dia = fechaObj.getDate().toString().padStart(2, '0');
                       const fechaFormateada = `${anio}.${mes}.${dia}`;

                       li.innerHTML = `<strong>${n.usuarioEmisor}</strong>: ${n.mensaje} <span class="fecha">${fechaFormateada}</span>`;
                       if (n.leida) li.classList.add('leida');

                       li.addEventListener('click', () => {
                           fetch(`/spring/marcar-leida/${n.id}`, { method: 'POST' })
                               .then(() => {
                                   const badge = btnNotificaciones.querySelector('.badge');
                                   if (badge) {
                                       let c = parseInt(badge.textContent);
                                       c = Math.max(0, c - 1);
                                       badge.textContent = c;
                                       if (c === 0) badge.style.display = 'none';
                                   }
                                   cargarNotificaciones();
                               })
                               .catch(err => console.error(err));
                       });

                       lista.appendChild(li);
                   });

                }
            })
            .catch(err => console.error(err));
    }
});

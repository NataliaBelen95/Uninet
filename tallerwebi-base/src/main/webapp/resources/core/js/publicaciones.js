document.addEventListener("DOMContentLoaded", function () {
    console.log("✅ JS listo y cargado");

    // Configuración de PDF.js
    pdfjsLib.GlobalWorkerOptions.workerSrc = 'https://cdnjs.cloudflare.com/ajax/libs/pdf.js/2.14.305/pdf.worker.min.js';

    // 🔄 WebSocket - Actualizar contador de likes y comentarios en tiempo real
    const socket = new SockJS('/spring/ws');
    const stompClient = Stomp.over(socket);
    stompClient.debug = null;

    stompClient.connect({}, function (frame) {
        console.log("🟢 Conectado al WebSocket:", frame);

        // Suscribirse a publicaciones visibles
        document.querySelectorAll('[id^="publicacion-"]').forEach(publi => {
            const publicacionId = publi.id.replace('publicacion-', '');
            const topic = `/topic/publicacion/${publicacionId}`;

            stompClient.subscribe(topic, function (message) {
                if (message.body === "comentarioNuevo") {
                    // Si llega notificación de nuevo comentario, recargar la publicación
                    actualizarPublicacion(publicacionId);
                } else {
                    // Asumiendo que viene la cantidad de likes
                    const nuevosLikes = JSON.parse(message.body);
                    actualizarLikes(publicacionId, nuevosLikes);
                }
            });
        });

        // Suscribirse al topic de nuevas publicaciones
        stompClient.subscribe('/topic/publicaciones', function (message) {
            const dto = JSON.parse(message.body);
            const publicacionId = dto.id;
            console.log('📩 ID de nueva publicación recibida:', publicacionId);

            // Cargar la nueva publicación
            fetch(`/spring/publicacion/tarjeta/${publicacionId}`)
                .then(response => response.text())
                .then(fragmentoHTML => {
                    const contenedorPublicaciones = document.getElementById("contenedor-publicaciones");
                    contenedorPublicaciones.insertAdjacentHTML('afterbegin', fragmentoHTML);

                    // Nueva suscripción para la publicación recién llegada
                    suscribirAActualizaciones(publicacionId);
                })
                .catch(error => console.error(" Error al cargar nueva publicación:", error));
        });
    });

    // Función para inicializar eventos
    function inicializarEventos() {
        // Mostrar/Ocultar contenedor de comentarios al hacer clic en el botón "comentar"
        document.querySelectorAll(".comentarBtn").forEach(btn => {
            btn.onclick = () => {
                const id = btn.dataset.id;
                const contenedor = document.getElementById('contenedor-comentar-' + id);
                if (!contenedor) return;

                // Alternar la visibilidad del contenedor de comentarios
                contenedor.style.display = (contenedor.style.display === "none" || contenedor.style.display === "") ? "block" : "none";
            };
        });

        // Envío de formulario de comentario
        document.querySelectorAll('.formComentario').forEach(formularioComentario => {
            formularioComentario.addEventListener('submit', function(event) {
                event.preventDefault();  // Evita que se recargue la página
                console.log("Formulario enviado para publicación ID: ", formularioComentario.getAttribute('id'));

                // Extraer texto del comentario
                const textoComentario = formularioComentario.querySelector('textarea').value;

                // Extraer ID de la publicación desde el ID del formulario: "form-comentario-123"
                const idPublicacion = formularioComentario.getAttribute('id').split('-')[2];

                // Hacer el POST con JSON
                fetch(`/spring/publicacion/comentar/${idPublicacion}`, {
                    method: 'POST',
                    headers: {
                        'X-Requested-With': 'XMLHttpRequest',
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({ texto: textoComentario })
                })
                .then(response => {
                    console.log("Código de respuesta:", response.status);
                    if (!response.ok) {
                        throw new Error(`Error al agregar el comentario. Código de error: ${response.status}`);
                    }
                    return response.json();
                })
                .then(data => {
                    console.log("Comentario agregado correctamente:", data);

                    // Actualizar el contador de comentarios
                    actualizarContadorComentarios(idPublicacion, data.cantidadComentarios);

                    // Agregar el comentario al DOM
                    agregarComentarioAlDOM(idPublicacion, data.comentario);
                })
                .catch(error => {
                    console.error("Error al enviar el comentario:", error);
                });
            });
        });

        // Función para dar like
       document.querySelectorAll(".likeBtn").forEach(btn => {
           btn.onclick = () => {
               const publicacionId = btn.dataset.id;
               console.log(`Dar like a la publicación con ID: ${publicacionId}`);

               // Realizar la solicitud fetch para dar like
               fetch(`/spring/publicacion/darLike/${publicacionId}`, {
                   method: 'POST',
                   headers: {
                       'X-Requested-With': 'XMLHttpRequest',

                   },
               })
               .then(response => {
                   // Si la respuesta es ok (código 2xx), continuar
                   if (response.ok) {
                       return response.text(); // Cambiar a .text() para recibir HTML como cadena
                   }
                   throw new Error('Error en la solicitud');
               })
               .then(html => {
                   // Crear un contenedor temporal para parsear el HTML
                   const parser = new DOMParser();
                   const doc = parser.parseFromString(html, 'text/html');

                   // Buscar el elemento que contiene la cantidad de likes en el fragmento HTML
                   const nuevoLikeSpan = doc.querySelector('.likes-count');  // Asegúrate de que esta clase exista en el HTML devuelto

                   if (nuevoLikeSpan) {
                       const nuevosLikes = nuevoLikeSpan.innerText;
                       console.log("Cantidad de likes actualizada:", nuevosLikes);

                       // Actualizar el contador de likes en el DOM
                       actualizarLikes(publicacionId, nuevosLikes);
                   }
               })
               .catch(error => {
                   console.error("Error al dar like:", error);
               });
           };
       });

        // Mostrar PDF
        document.querySelectorAll(".ver-pdf-btn").forEach(btn => {
            btn.onclick = () => {
                const url = btn.dataset.url;  // Obtiene la URL del PDF (dinámica)
                mostrarPDF(url);  // Llama a la función mostrarPDF
            };
        });

        // Mostrar/Ocultar contenedor de comentarios al hacer clic en el botón "ver comentarios"
       document.querySelectorAll(".ver-comentariosBtn").forEach(btn => {
           btn.onclick = () => {
               const id = btn.dataset.id;
               const contenedorComentarios = document.getElementById('contenedor-comentarios-' + id);
               if (!contenedorComentarios) return;

               // Verificar si ya están cargados los comentarios
               if (!contenedorComentarios.hasAttribute('data-loaded')) {
                   // Si no están cargados, cargamos los comentarios desde el servidor
                   fetch(`/spring/publicacion/comentarios/${id}`)
                       .then(response => response.json())
                       .then(data => {
                            contenedorComentarios.innerHTML = '';

                           data.comentarios.forEach(comentario => {
                               agregarComentarioAlDOM(id, comentario);
                           });

                           // Marcar como cargados
                           contenedorComentarios.setAttribute('data-loaded', 'true');
                       })
                       .catch(error => console.error("Error al cargar comentarios:", error));
               }

               // Alternar visibilidad de los comentarios
               contenedorComentarios.style.display = (contenedorComentarios.style.display === "none" || contenedorComentarios.style.display === "") ? "block" : "none";
           };
       });

        // Adjuntar archivo desde el input
        const botonAdjuntar = document.querySelector('button[name="archivo"]');
        const inputArchivo = document.querySelector('input[name="archivos"]');

        if (botonAdjuntar && inputArchivo) {
            botonAdjuntar.addEventListener('click', function () {
                console.log("CLICK EN BOTÓN DE ADJUNTAR");
                inputArchivo.click();  // Abre el selector de archivos
            });

            inputArchivo.addEventListener('change', function () {
                const archivos = inputArchivo.files;
                if (archivos.length > 0) {
                    console.log(`Archivo seleccionado: ${archivos[0].name}`);
                    // Aquí puedes manejar la subida del archivo si lo deseas
                    // También podrías mostrar un mensaje confirmando que el archivo fue seleccionado
                }
            });
        } else {
            console.log("NO se encontró el botón o el input para adjuntar");
        }
    }

    // Función para agregar el comentario al DOM
    function agregarComentarioAlDOM(publicacionId, comentario) {
        const contenedorComentarios = document.getElementById('contenedor-comentarios-' + publicacionId);
        if (!contenedorComentarios) {
            console.error('No se encontró el contenedor de comentarios para la publicación con ID:', publicacionId);
            return;
        }

        console.log('Contenedor de comentarios encontrado:', contenedorComentarios);

        // Crear un nuevo comentario
        const nuevoComentario = document.createElement('div');
        nuevoComentario.classList.add('comentario');
        nuevoComentario.innerHTML = `
            <p><strong>${comentario.nombreUsuario} ${comentario.apellidoUsuario}</strong></p>
            <p>${comentario.texto}</p>
        `;

        // Agregar el nuevo comentario al principio del contenedor
        contenedorComentarios.insertAdjacentElement('afterbegin', nuevoComentario);

        // Verificar si el contenedor tiene la clase 'mostrar'
        if (!contenedorComentarios.classList.contains('mostrar')) {
            contenedorComentarios.classList.add('mostrar'); // Asegura que el contenedor se muestre
        }

        // Asegurar que el nuevo comentario sea visible (por defecto, 'display: none' en CSS)
        nuevoComentario.style.display = "block";  // Mostrar el comentario
    }

    // Función para mostrar el PDF (abre en nueva pestaña)
    function mostrarPDF(url) {
        console.log(`Abriendo PDF desde la URL: ${url}`);
        window.open(url, '_blank');  // Abre el archivo en una nueva pestaña
    }

    // Actualizar los likes
    function actualizarLikes(publicacionId, nuevosLikes) {
        const likeSpan = document.getElementById('likes-' + publicacionId);
        if (likeSpan) {
            likeSpan.innerText = nuevosLikes;
        }
    }

    // Actualizar el contador de comentarios
    function actualizarContadorComentarios(id, cantidadComentarios) {
        const comentarioBtn = document.querySelector(`[data-id="${id}"].ver-comentariosBtn`);
        if (comentarioBtn) {
            comentarioBtn.innerText = cantidadComentarios === 1 ? '1 Comentario' : `${cantidadComentarios} Comentarios`;
        }
    }

    // Actualizar la publicación completa
    function actualizarPublicacion(publicacionId) {
        fetch(`/spring/publicacion/tarjeta/${publicacionId}`)
            .then(response => response.text())
            .then(fragmentoHTML => {
                const contenedorPublicacion = document.getElementById('publicacion-' + publicacionId);
                if (contenedorPublicacion) {
                    contenedorPublicacion.outerHTML = fragmentoHTML;
                }
                inicializarEventos(); // Re-inicializar eventos en el nuevo contenido
            })
            .catch(error => console.error("❌ Error al actualizar la publicación:", error));
    }

    // Suscribir a actualizaciones de una publicación específica
    function suscribirAActualizaciones(publicacionId) {
        const topic = `/topic/publicacion/${publicacionId}`;
        stompClient.subscribe(topic, function (message) {
            if (message.body === "comentarioNuevo") {
                actualizarPublicacion(publicacionId);
            } else {
                const nuevosLikes = JSON.parse(message.body);
                actualizarLikes(publicacionId, nuevosLikes);
            }
        });
    }

    // Inicializar eventos al cargar la página
    inicializarEventos();
});

document.addEventListener("DOMContentLoaded", function () {
    console.log("✅ JS listo y cargado");


    // Configuración de PDF.js
    pdfjsLib.GlobalWorkerOptions.workerSrc = 'https://cdnjs.cloudflare.com/ajax/libs/pdf.js/2.14.305/pdf.worker.min.js';

    // 🔄 WebSocket - Actualizar contador de likes y comentarios en tiempo real
    const socket = new SockJS('/spring/ws');
    const stompClient = Stomp.over(socket);
    stompClient.debug = null;

    // Para controlar suscripciones y evitar duplicados
    const suscripcionesPublicacion = new Set();

    stompClient.connect({}, function (frame) {
        console.log("🟢 Conectado al WebSocket:", frame);

        // Suscribirse a publicaciones visibles actuales
        document.querySelectorAll('[id^="publicacion-"]').forEach(publi => {
            const publicacionId = publi.id.replace('publicacion-', '');
            suscribirAActualizaciones(publicacionId);
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
                    inicializarEventos();

                    // Nueva suscripción para la publicación recién llegada
                    suscribirAActualizaciones(publicacionId);
                })
                .catch(error => console.error("Error al cargar nueva publicación:", error));
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

                contenedor.style.display = (contenedor.style.display === "none" || contenedor.style.display === "") ? "block" : "none";
            };
        });

        // Envío de formulario de comentario
        document.querySelectorAll('.formComentario').forEach(formularioComentario => {
            formularioComentario.addEventListener('submit', function (event) {
                event.preventDefault();  // Evita recarga de página
                console.log("Formulario enviado para publicación ID:", formularioComentario.getAttribute('id'));

                const textoComentario = formularioComentario.querySelector('textarea').value;
                const idPublicacion = formularioComentario.getAttribute('id').split('-')[2];

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

                        actualizarContadorComentarios(idPublicacion, data.cantidadComentarios);
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

                fetch(`/spring/publicacion/darLike/${publicacionId}`, {
                    method: 'POST',
                    headers: {
                        'X-Requested-With': 'XMLHttpRequest',
                    },
                })
                    .then(response => {
                        if (response.ok) {
                            return response.text();
                        }
                        throw new Error('Error en la solicitud');
                    })
                    .then(html => {
                        const parser = new DOMParser();
                        const doc = parser.parseFromString(html, 'text/html');
                        const nuevoLikeSpan = doc.querySelector('.likes-count');

                        if (nuevoLikeSpan) {
                            const nuevosLikes = nuevoLikeSpan.innerText;
                            console.log("Cantidad de likes actualizada:", nuevosLikes);
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
                const url = btn.dataset.url;
                mostrarPDF(url);
            };
        });

        // Mostrar/Ocultar contenedor de comentarios al hacer clic en "ver comentarios"
        document.querySelectorAll(".ver-comentariosBtn").forEach(btn => {
            btn.onclick = () => {
                const id = btn.dataset.id;
                const contenedorComentarios = document.getElementById('contenedor-comentarios-' + id);
                if (!contenedorComentarios) return;

                if (!contenedorComentarios.hasAttribute('data-loaded')) {
                    fetch(`/spring/publicacion/comentarios/${id}`)
                        .then(response => response.json())
                        .then(data => {
                            contenedorComentarios.innerHTML = '';
                            data.comentarios.forEach(comentario => {
                                agregarComentarioAlDOM(id, comentario);
                            });
                            contenedorComentarios.setAttribute('data-loaded', 'true');
                        })
                        .catch(error => console.error("Error al cargar comentarios:", error));
                }

                contenedorComentarios.style.display = (contenedorComentarios.style.display === "none" || contenedorComentarios.style.display === "") ? "block" : "none";
            };
        });

        // Adjuntar archivo desde input
       const botonAdjuntar = document.getElementById('botonAdjuntar');
          const inputArchivo = document.getElementById('inputArchivo');

        if (botonAdjuntar && inputArchivo) {
            botonAdjuntar.addEventListener('click', () => {
                console.log("CLICK EN BOTÓN DE ADJUNTAR");
                inputArchivo.click();
            });

            inputArchivo.addEventListener('change', () => {
                const archivos = inputArchivo.files;
                if (archivos.length > 0) {
                    console.log(`Archivo seleccionado: ${archivos[0].name}`);
                    // Puedes manejar la subida del archivo aquí si quieres
                }
            });
        } else {
            console.log("NO se encontró el botón o el input para adjuntar");
        }
    }

    // Agregar comentario al DOM
    function agregarComentarioAlDOM(publicacionId, comentario) {
        const contenedorComentarios = document.getElementById('contenedor-comentarios-' + publicacionId);
        if (!contenedorComentarios) {
            console.error('No se encontró el contenedor de comentarios para la publicación con ID:', publicacionId);
            return;
        }

        const nuevoComentario = document.createElement('div');
        nuevoComentario.classList.add('comentario');
        nuevoComentario.innerHTML = `
            <p><strong>${comentario.nombreUsuario} ${comentario.apellidoUsuario}</strong></p>
            <p>${comentario.texto}</p>
        `;

        contenedorComentarios.insertAdjacentElement('afterbegin', nuevoComentario);

        if (!contenedorComentarios.classList.contains('mostrar')) {
            contenedorComentarios.classList.add('mostrar');
        }

        nuevoComentario.style.display = "block";
    }

    // Mostrar PDF en nueva pestaña
    function mostrarPDF(url) {
        console.log(`Abriendo PDF desde la URL: ${url}`);
        window.open(url, '_blank');
    }

    // Actualizar likes en el DOM
    function actualizarLikes(publicacionId, nuevosLikes) {
        const likeSpan = document.getElementById('likes-' + publicacionId);
        if (likeSpan) {
            likeSpan.innerText = nuevosLikes;
        }
    }

    // Actualizar contador de comentarios
    function actualizarContadorComentarios(id, cantidadComentarios) {
        const comentarioBtn = document.querySelector(`[data-id="${id}"].ver-comentariosBtn`);
        if (comentarioBtn) {
            comentarioBtn.innerText = cantidadComentarios === 1 ? '1 Comentario' : `${cantidadComentarios} Comentarios`;
        }
    }

    // Actualizar toda la publicación (HTML)
    function actualizarPublicacion(publicacionId) {
        fetch(`/spring/publicacion/tarjeta/${publicacionId}`)
            .then(response => response.text())
            .then(fragmentoHTML => {
                const contenedorPublicacion = document.getElementById('publicacion-' + publicacionId);
                if (contenedorPublicacion) {
                    contenedorPublicacion.outerHTML = fragmentoHTML;
                }
                inicializarEventos();
            })
            .catch(error => console.error("Error al actualizar la publicación:", error));
    }

    // Suscribir a actualizaciones específicas (likes y comentarios)
    function suscribirAActualizaciones(publicacionId) {
        if (suscripcionesPublicacion.has(publicacionId)) {
            return; // Ya suscripto
        }

        const topic = `/topic/publicacion/${publicacionId}`;
        stompClient.subscribe(topic, function (message) {
            if (message.body === "comentarioNuevo") {
                actualizarPublicacion(publicacionId);
            } else {
                try {
                    const nuevosLikes = JSON.parse(message.body);
                    actualizarLikes(publicacionId, nuevosLikes);
                } catch {
                    console.warn("Mensaje recibido no es JSON esperado para likes:", message.body);
                }
            }
        });

        suscripcionesPublicacion.add(publicacionId);
    }

    // Inicializar eventos al cargar la página
    inicializarEventos();
});

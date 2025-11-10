document.addEventListener("DOMContentLoaded", function () {
    console.log("✅ JS listo y cargado");
     

    // ----------------------
    // PDF.js
    // ----------------------
    if (typeof pdfjsLib !== "undefined") {
        pdfjsLib.GlobalWorkerOptions.workerSrc = 'https://cdnjs.cloudflare.com/ajax/libs/pdf.js/2.14.305/pdf.worker.min.js';
    }

    // ----------------------
    // WebSocket
    // ----------------------
    const socket = new SockJS('/spring/ws');
    const stompClient = Stomp.over(socket);
    stompClient.debug = null;

    const suscripcionesPublicacion = new Set();

    stompClient.connect({}, function (frame) {
        console.log("🟢 Conectado al WebSocket:", frame);

        // Suscribirse a publicaciones ya visibles
        document.querySelectorAll('[id^="publicacion-"]').forEach(publi => {
            const publicacionId = publi.id.replace('publicacion-', '');
            suscribirAActualizaciones(publicacionId);
        });

        // Suscribirse a nuevas publicaciones
        stompClient.subscribe('/topic/publicaciones', function (message) {
            const dto = JSON.parse(message.body);
            const publicacionId = dto.id;
            console.log('📩 Nueva publicación:', publicacionId);

            fetch(`/spring/publicacion/tarjeta/${publicacionId}`)
                .then(res => res.text())
                .then(fragmentoHTML => {
                    const contenedor = document.getElementById("contenedor-publicaciones");
                    if (contenedor) {
                        contenedor.insertAdjacentHTML('afterbegin', fragmentoHTML);
                    }
                    inicializarEventos();
                    suscribirAActualizaciones(publicacionId);
                })
                .catch(err => console.error("Error al cargar publicación:", err));
        });
    });

    // ----------------------
    // Inicialización de eventos
    // ----------------------
    function inicializarEventos() {
        // ---- Botones de like ----
        document.querySelectorAll(".likeBtn").forEach(btn => {
            btn.onclick = () => {
                const publicacionId = btn.dataset.id;
                darLike(publicacionId);
            };
        });

        // ---- Botones comentar ----
        document.querySelectorAll(".comentarBtn").forEach(btn => {
            btn.onclick = () => {
                const id = btn.dataset.id;
                toggleContenedor("contenedor-comentar-" + id);
            };
        });

        // ---- Ver comentarios ----
        document.querySelectorAll(".ver-comentariosBtn").forEach(btn => {
            btn.onclick = () => {
                const id = btn.dataset.id;
                toggleContenedorComentarios(id);
            };
        });

        // ---- Formulario de comentarios ----
        document.querySelectorAll('.formComentario').forEach(formulario => {
            formulario.addEventListener('submit', function (event) {
                event.preventDefault();
                const idPublicacion = formulario.id.split('-')[2];
                const textoComentario = formulario.querySelector('textarea').value;
                enviarComentario(idPublicacion, textoComentario);
            });
        });

        // ---- Ver PDF ----
        document.querySelectorAll(".ver-pdf-btn").forEach(btn => {
            btn.onclick = () => {
                const url = btn.dataset.url;
                mostrarPDF(url);
            };
        });

        // ---- Adjuntar archivo ---
        const botonAdjuntar = document.getElementById('botonAdjuntar');
        const inputArchivo = document.getElementById('inputArchivo');

        if (botonAdjuntar && inputArchivo) {
            botonAdjuntar.onclick = () => inputArchivo.click();

            inputArchivo.onchange = () => {
                if (inputArchivo.files.length > 0) {
                    console.log("Archivo seleccionado:", inputArchivo.files[0].name);
                    // Aquí podés manejar la subida si querés
                }
            };
        }
    }

    // ----------------------
    // Funciones de interacción
    // ----------------------
    function darLike(publicacionId) {
        fetch(`/spring/publicacion/darLike/${publicacionId}`, {
            method: 'POST',
            headers: { 'X-Requested-With': 'XMLHttpRequest' }
        })
        .then(res => res.text())
        .then(html => {
            const parser = new DOMParser();
            const doc = parser.parseFromString(html, 'text/html');
            const nuevoLikeSpan = doc.querySelector('.likes-count');
            if (nuevoLikeSpan) actualizarLikes(publicacionId, nuevoLikeSpan.innerText);
        })
        .catch(err => console.error("Error dar like:", err));
    }

    function toggleContenedor(id) {
        const cont = document.getElementById(id);
        if (cont) cont.style.display = (cont.style.display === "block") ? "none" : "block";
    }

    function toggleContenedorComentarios(id) {
        const cont = document.getElementById("contenedor-comentarios-" + id);
        if (!cont) return;

        if (!cont.hasAttribute('data-loaded')) {
            fetch(`/spring/publicacion/comentarios/${id}`)
                .then(res => res.json())
                .then(data => {
                    cont.innerHTML = '';
                    data.comentarios.forEach(c => agregarComentarioAlDOM(id, c));
                    cont.setAttribute('data-loaded', 'true');
                })
                .catch(err => console.error("Error al cargar comentarios:", err));
        }

        cont.style.display = (cont.style.display === "block") ? "none" : "block";
    }

    function enviarComentario(publicacionId, texto) {
        fetch(`/spring/publicacion/comentar/${publicacionId}`, {
            method: 'POST',
            headers: {
                'X-Requested-With': 'XMLHttpRequest',
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ texto })
        })
        .then(res => res.json())
        .then(data => {
            agregarComentarioAlDOM(publicacionId, data.comentario);
            actualizarContadorComentarios(publicacionId, data.cantidadComentarios);
        })
        .catch(err => console.error("Error enviar comentario:", err));
    }

    function agregarComentarioAlDOM(publicacionId, comentario) {
        const cont = document.getElementById('contenedor-comentarios-' + publicacionId);
        if (!cont) return;
        const div = document.createElement('div');
        div.classList.add('comentario');
        div.innerHTML = `<p><strong>${comentario.nombreUsuario} ${comentario.apellidoUsuario}</strong></p><p>${comentario.texto}</p>`;
        cont.insertAdjacentElement('afterbegin', div);
    }

    function mostrarPDF(url) {
        window.open(url, '_blank');
    }

    function actualizarLikes(publicacionId, nuevosLikes) {
        const span = document.getElementById('likes-' + publicacionId);
        if (span) span.innerText = nuevosLikes;
    }

    function actualizarContadorComentarios(publicacionId, cantidad) {
        const btn = document.querySelector(`[data-id="${publicacionId}"].ver-comentariosBtn`);
        if (btn) btn.innerText = cantidad === 1 ? '1 Comentario' : `${cantidad} Comentarios`;
    }

    function actualizarPublicacion(publicacionId) {
        fetch(`/spring/publicacion/tarjeta/${publicacionId}`)
            .then(res => res.text())
            .then(html => {
                const cont = document.getElementById('publicacion-' + publicacionId);
                if (cont) cont.outerHTML = html;
                inicializarEventos();
            })
            .catch(err => console.error("Error actualizar publicación:", err));
    }

    function suscribirAActualizaciones(publicacionId) {
        if (suscripcionesPublicacion.has(publicacionId)) return;

        stompClient.subscribe(`/topic/publicacion/${publicacionId}`, function (message) {
            if (message.body === "comentarioNuevo") {
                actualizarPublicacion(publicacionId);
            } else {
                try {
                    const nuevosLikes = JSON.parse(message.body);
                    actualizarLikes(publicacionId, nuevosLikes);
                } catch {}
            }
        });

        suscripcionesPublicacion.add(publicacionId);
    }

    // Inicializar eventos de inicio
    inicializarEventos();
});

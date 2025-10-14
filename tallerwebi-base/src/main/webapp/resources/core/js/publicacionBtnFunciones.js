document.addEventListener("DOMContentLoaded", function () {
    console.log("JS listo y cargado");

    // Configurar worker para PDF.js (misma versión que el script)
    pdfjsLib.GlobalWorkerOptions.workerSrc = 'https://cdnjs.cloudflare.com/ajax/libs/pdf.js/2.14.305/pdf.worker.min.js';

    function inicializarEventos() {
        // Botones comentar
        document.querySelectorAll(".comentarBtn").forEach(btn => {
            btn.onclick = () => {
                const id = btn.dataset.id;
                const contenedor = document.getElementById('contenedor-comentar-' + id);

                if (!contenedor) {
                    console.log("Contenedor no encontrado para comentar id:", id);
                    return;
                }

                // Toggling visibility of the comment form
                contenedor.style.display = (contenedor.style.display === "none" || contenedor.style.display === "") ? "block" : "none";

                // Attach submit event handler for the comment form
                const form = document.querySelector(`#form-comentario-${id}`);
                if (form) {
                    form.onsubmit = (e) => {
                        e.preventDefault(); // Prevent normal form submission

                        const textarea = form.querySelector("textarea");
                        const textoComentario = textarea.value.trim();

                        if (!textoComentario) return;  // No empty comments

                        // Fetch request to add the comment
                        fetch('/spring/publicacion/comentar/' + id, {
                            method: 'POST',
                            headers: {
                                'X-Requested-With': 'XMLHttpRequest',
                                'Content-Type': 'application/x-www-form-urlencoded'
                            },
                            body: new URLSearchParams({
                                'texto': textoComentario
                            })
                        })
                        .then(response => response.text())
                        .then(htmlFragment => {
                            const el = document.getElementById('publicacion-' + id);  // Publicación con id "publicacion-{id}"
                            if (!el) return;
                            el.outerHTML = htmlFragment;  // Actualiza la publicación con el nuevo comentario
                            inicializarEventos();  // Reasigna los eventos de los botones
                        })
                        .catch(err => console.error("Error al comentar:", err));
                    };
                }
            };
        });

        // Botones ver comentarios
        document.querySelectorAll(".ver-comentariosBtn").forEach(btn => {
            btn.onclick = () => {
                const id = btn.dataset.id;
                const contenedor = document.getElementById('contenedor-comentarios-' + id);
                if (!contenedor) {
                    console.log("Contenedor de comentarios no encontrado para id:", id);
                    return;
                }
                contenedor.style.display = (contenedor.style.display === "none" || contenedor.style.display === "") ? "block" : "none";
            };
        });

        // Botones like
        document.querySelectorAll(".likeBtn").forEach(btn => {
            btn.onclick = () => {
                const id = btn.dataset.id;
                if (!id) return;
                fetch('/spring/publicacion/darLike/' + id, {
                    method: 'POST',
                    headers: { 'X-Requested-With': 'XMLHttpRequest' }
                })
                .then(response => response.text())
                .then(htmlFragment => {
                    const el = document.getElementById('publicacion-' + id);
                    if (!el) return;
                    el.outerHTML = htmlFragment;
                    inicializarEventos(); // Reasignar eventos
                })
                .catch(err => console.error("Error al dar like:", err));
            };
        });

        // Botones ver PDF
        document.querySelectorAll(".ver-pdf-btn").forEach(btn => {
            btn.onclick = () => {
                const id = btn.dataset.id;
                const url = btn.dataset.url;
                mostrarPDF(id, url);
            };
        });
    }

    // Función para mostrar u ocultar PDF
    window.mostrarPDF = function(id, ruta) {
        const container = document.getElementById('pdf-container-' + id);
        const canvas = document.getElementById('pdf-viewer-' + id);
        if (!container || !canvas) {
            console.error("Contenedor o canvas no encontrado para id:", id);
            return;
        }

        if (container.style.display === 'block') {
            container.style.display = 'none';
            return;
        }

        container.style.display = 'block';

        const loadingTask = pdfjsLib.getDocument(ruta);
        loadingTask.promise.then(pdf => {
            return pdf.getPage(1);
        }).then(page => {
           const containerWidth = container.clientWidth;
           const unscaledViewport = page.getViewport({ scale: 1 });
           const scale = (containerWidth * 0.8) / unscaledViewport.width;
           const viewport = page.getViewport({ scale });
           const context = canvas.getContext('2d');
               canvas.width = viewport.width;
               canvas.height = viewport.height

            const renderContext = {
                canvasContext: context,
                viewport: viewport
            };
            page.render(renderContext);
        }).catch(err => {
            console.error("Error cargando PDF:", err);
        });
    };

    // Inicializar todos los eventos al cargar
    inicializarEventos();

    // Botón para input archivo
    const archivoBtn = document.querySelector('button[name="archivo"]');
    const archivoInput = document.querySelector('input[type="file"][name="archivos"]');

    if (archivoBtn && archivoInput) {
        archivoBtn.addEventListener('click', () => {
            archivoInput.click();
        });
    }
});
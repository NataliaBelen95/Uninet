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
                contenedor.style.display = (contenedor.style.display === "none" || contenedor.style.display === "") ? "block" : "none";
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
            const viewport = page.getViewport({ scale: 1.0 });
            const context = canvas.getContext('2d');
            canvas.height = viewport.height;
            canvas.width = viewport.width;

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

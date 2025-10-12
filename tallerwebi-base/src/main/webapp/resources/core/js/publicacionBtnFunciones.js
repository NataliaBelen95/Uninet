document.addEventListener("DOMContentLoaded", function () {
    console.log("Script cargado");

    // ====== FUNCIÓN PARA REASIGNAR EVENTOS ======
    function inicializarEventos() {
        // Mostrar/Ocultar comentarios
        document.querySelectorAll(".ver-comentariosBtn").forEach(function(btn) {
            btn.onclick = function() {
                const id = btn.dataset.id;
                if (!id) return;

                if (btn.textContent.trim().includes("No hay comentarios")) return;

                const contenedor = document.getElementById('contenedor-comentarios-' + id);
                if (contenedor) {
                    contenedor.style.display = contenedor.style.display === "none" ? "block" : "none";
                }
            };
        });

        // Mostrar/Ocultar formulario de comentario
        document.querySelectorAll(".comentarBtn").forEach(function(btn) {
            btn.onclick = function() {
                console.log("click comentar");
                const id = btn.dataset.id;
                if (!id) return;

                const form = document.getElementById('form-comentario-' + id);
                if (!form) return;

                form.style.display = (form.style.display === "none" || form.style.display === "") ? "block" : "none";
                if (form.style.display === "block") form.querySelector("textarea").focus();
            };
        });
    }

    // ====== DAR LIKE ======
    window.darLike = function(id) {


        fetch('/spring/publicacion/darLike/' + id, {
            method: 'POST',
            headers: { 'X-Requested-With': 'XMLHttpRequest' }
        })
        .then(response => response.text())
        .then(htmlFragment => {
            const el = document.getElementById('publicacion-' + id);
            if (!el) return;
            el.outerHTML = htmlFragment;

            inicializarEventos();
        })
        .catch(err => console.error(err));
    };

    // Inicializar eventos la primera vez
    inicializarEventos();
});
document.addEventListener("DOMContentLoaded", function () {
    console.log("JS listo y cargado");

    function inicializarEventos() {
        // Botones comentar
document.querySelectorAll(".comentarBtn").forEach(btn => {
    btn.onclick = () => {
        const id = btn.dataset.id;
        console.log("Click comentar id:", id);
        const contenedor = document.getElementById('contenedor-comentar-' + id);
        if (!contenedor) {
            console.log("Contenedor no encontrado");
            return;
        }
        contenedor.style.display = (contenedor.style.display === "none" || contenedor.style.display === "") ? "block" : "none";
        console.log("Contenedor display:", contenedor.style.display);
    };
});


document.querySelectorAll(".ver-comentariosBtn").forEach(btn => {
    btn.onclick = () => {
        const id = btn.dataset.id;
        if (!id) return;

        const contenedor = document.getElementById('contenedor-comentarios-' + id);
        if (!contenedor) {
            console.log("Contenedor de comentarios no encontrado para id:", id);
            return;
        }
        contenedor.style.display = (contenedor.style.display === "none" || contenedor.style.display === "") ? "block" : "none";
    };
});


        // Botones like
        document.querySelectorAll(".likeBtn").forEach(function(btn) {
            btn.onclick = function() {
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

                    // Reasignar eventos para nuevos elementos
                    inicializarEventos();
                })
                .catch(err => console.error("Error al dar like:", err));
            };
        });
    }

    // Ejecutar por primera vez la asignación de eventos
    inicializarEventos();
});

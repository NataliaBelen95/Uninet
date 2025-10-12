document.addEventListener("DOMContentLoaded", function () {
    console.log("Script cargado");

    // Mostrar comentarios
   document.querySelectorAll(".ver-comentariosBtn").forEach(function (btn) {
       btn.addEventListener("click", function () {
           // Si el texto contiene "No comentarios", no hace nada
           if (btn.textContent.trim().includes("No comentarios")) {
               return;
           }

           const contenedor = btn.closest("article").querySelector(".contenedor_comentarios");
           if (contenedor) {
               contenedor.style.display = contenedor.style.display === "none" ? "block" : "none";
           }
       });
   });

    // Mostrar/Ocultar formulario para comentar
    document.querySelectorAll(".comentarBtn").forEach(function (btn) {
        btn.addEventListener("click", function () {
            const form = btn.parentElement.querySelector(".formComentario");
            if (form) {
                if (form.style.display === "none" || form.style.display === "") {
                    form.style.display = "block";
                    form.querySelector("textarea").focus();
                } else {
                    form.style.display = "none";
                }
            }
        });
    });
});

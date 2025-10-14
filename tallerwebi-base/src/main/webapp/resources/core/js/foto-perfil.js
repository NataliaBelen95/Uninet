// foto-perfil.js
document.addEventListener('DOMContentLoaded', function () {
    const popUp = document.getElementById("popUp");
    const perfilFoto = document.getElementById("perfilFoto");
    const closeButton = document.querySelector(".popup-cerrar");
    const inputFoto = document.getElementById("inputFoto");
    const fotoPreview = document.getElementById("foto-preview");

    function abrirPopUp() {
        if (popUp) popUp.style.display = "block";
        // set preview from current image if exists
        if (perfilFoto && fotoPreview) {
            fotoPreview.src = perfilFoto.src;
        }
    }

    function cerrarPopUp() {
        if (popUp) popUp.style.display = "none";
    }

    function mostrarPreview(event) {
        const file = event.target.files && event.target.files[0];
        if (!file) return;
        const reader = new FileReader();
        reader.onload = function() {
            if (fotoPreview) fotoPreview.src = reader.result;
        };
        reader.readAsDataURL(file);
    }

    if (perfilFoto) perfilFoto.addEventListener("click", abrirPopUp);
    if (closeButton) closeButton.addEventListener("click", cerrarPopUp);
    if (inputFoto) inputFoto.addEventListener("change", mostrarPreview);

    // click fuera del contenido cierra popup
    window.addEventListener('click', function(e){
        if (e.target === popUp) cerrarPopUp();
    });
     // Cierra el popup automáticamente al enviar cualquiera de los formularios
        const forms = document.querySelectorAll("#form-cambiar-foto, .form-eliminar-foto");
        forms.forEach(f => f.addEventListener("submit", () => {
            if (popUp) popUp.style.display = "none";
            //f.reset();
            //popUp.offsetHeight;
        }));
});

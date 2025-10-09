document.getElementById('formPublicacion').addEventListener('submit', function(e){

 let textarea = document.getElementById('publicacion');
        let btnPublicar = document.getElementById('btnPublicar');
        let archivo = document.getElementById('archivo');

       function actualizarBoton() {
       let texto = textarea.value.trim();
       let archivoSeleccionado = archivo.files.length > 0;

      // Habilitar si hay texto o hay un archivo
       btnPublicar.disabled = texto === '' && !archivoSeleccionado;
}

textarea.addEventListener('input', actualizarBoton);
archivo.addEventListener('change', actualizarBoton);
actualizarBoton(); // para inicializar el estado del botón
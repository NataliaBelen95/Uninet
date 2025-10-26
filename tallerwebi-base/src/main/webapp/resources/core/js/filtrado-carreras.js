
document.addEventListener("DOMContentLoaded", function () {
    //busco los elementos select del form
    const departamentoSelect = document.getElementById("departamento");
    const carreraSelect = document.getElementById("carrera");

    //cuando el usuario selecciona un depto ejecuto esto
    departamentoSelect.addEventListener("change", function () {
        //tomo el valor Id del seleccionado
        const departamentoId = this.value;
        //limpio el combo de carrera para que solo aparezca esto hasta que se traigan las correspondientes al depto
        carreraSelect.innerHTML = '<option value="">Seleccionar una Carrera</option>';
        carreraSelect.disabled = true;
        //si selecciono algo, trae lo que corresponde
        if (departamentoId) {
            fetch(`/spring/carreras-por-departamento?departamentoId=${departamentoId}`)

                .then(response => response.json())
                .then(data => {
                    data.forEach(carrera => {
                        const option = document.createElement("option");
                        option.value = carrera.id;
                        option.textContent = carrera.nombre;
                        carreraSelect.appendChild(option);
                    });
                    carreraSelect.disabled = false;
                })
                .catch(error => {
                    console.error("Error al cargar carreras:", error);
                });
        }
    });
});

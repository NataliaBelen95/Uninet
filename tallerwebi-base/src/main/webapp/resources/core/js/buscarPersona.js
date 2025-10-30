document.getElementById("searchInput").addEventListener("input", function() {
    var query = this.value;

    if (query.length >= 3) {  // Solo realizar búsqueda si hay 3 o más caracteres
        fetch(`/spring/buscarPersona?query=${query}`, {
            method: 'GET',
            headers: {
                'Accept': 'application/json'
            }
        })
        .then(response => response.json()) // Espera la respuesta como JSON
        .then(data => {
            // Obtener el dropdown y el contenedor de resultados
            let dropdown = document.getElementById("searchDropdown");
            let resultsDiv = document.getElementById("searchResults");

           if (data.length > 0) {
               // Si hay resultados, mostrar el dropdown
               resultsDiv.innerHTML = data.map(usuario => {
                   // Verificar si hay foto de perfil o usar la predeterminada
                   const fotoPerfil = usuario.fotoPerfil && usuario.fotoPerfil.trim() !== "" ? usuario.fotoPerfil : './imagenes/user.png';

                   return `
                       <a href="/spring/perfil/${usuario.slug}">
                           <img src="${fotoPerfil}" alt="Foto perfil" class="resultado-img"/>
                           <span>${usuario.nombre} ${usuario.apellido}</span>
                       </a>
                   `;
               }).join('');
               dropdown.style.display = 'block';
            } else {
                // Si no hay resultados, esconder el dropdown
                dropdown.style.display = 'none';
            }
        })
        .catch(error => console.error('Error al obtener los resultados:', error));
    } else {
        // Si el query tiene menos de 3 caracteres, esconder el dropdown
        document.getElementById("searchDropdown").style.display = 'none';
    }
});
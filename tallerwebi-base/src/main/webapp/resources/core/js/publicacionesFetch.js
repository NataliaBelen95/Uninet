window.darLike = function(id) {
  console.log("hola dar like");

  fetch('/spring/publicacion/darLike/' + id, {
    method: 'POST',
    headers: {
      'X-Requested-With': 'XMLHttpRequest'
    }
  })
  .then(response => {
    if (!response.ok) {
      throw new Error('Error al dar like');
    }
    return response.text();  // IMPORTANTE: retorna esta promesa
  })
  .then(htmlFragment => {
    console.log('Fragmento recibido:', htmlFragment);
    const el = document.getElementById('publicacion-' + id);
    if (!el) {
      console.error('No se encontró el elemento con id:', 'publicacion-' + id);
      return;
    }
    el.outerHTML = htmlFragment;
  })
  .catch(error => {
    console.error(error);
  });
};
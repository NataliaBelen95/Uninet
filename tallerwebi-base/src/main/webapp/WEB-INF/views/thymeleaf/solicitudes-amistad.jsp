<h2>Solicitudes pendientes</h2>
<c:forEach var="s" items="${solicitudes}">
    <div>
        <p>${s.solicitante.nombre} ${s.solicitante.apellido}</p>
        <form action="/spring/amistad/aceptar/${s.id}" method="post"><button>Aceptar</button></form>
        <form action="/spring/amistad/rechazar/${s.id}" method="post"><button>Rechazar</button></form>
    </div>
</c:forEach>

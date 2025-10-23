<h2>Mis amigos</h2>
<ul>
    <c:forEach var="a" items="${amigos}">
        <li>${a.nombre} ${a.apellido}</li>
    </c:forEach>
</ul>

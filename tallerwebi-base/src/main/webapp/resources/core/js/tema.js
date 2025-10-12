document.addEventListener("DOMContentLoaded", function () {
    const btnTema = document.getElementById("btn-tema");

    if (localStorage.getItem("tema") === "claro") {
        document.body.classList.add("tema-claro");
    }

    btnTema.addEventListener("click", () => {
        document.body.classList.toggle("tema-claro");

        if (document.body.classList.contains("tema-claro")) {
            localStorage.setItem("tema", "claro");
        } else {
            localStorage.setItem("tema", "oscuro");
        }
    });
});

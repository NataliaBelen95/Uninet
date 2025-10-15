window.addEventListener("load", function() {
    const links = document.querySelectorAll(".menu a");
    const currentUrl = window.location.pathname;

    links.forEach(link => {
        if (currentUrl.endsWith(link.getAttribute("href"))) {
            link.classList.add("active");
        }
    });
});

package com.tallerwebi.punta_a_punta.vistas;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;

public class VistaMiPerfil extends VistaWeb {

    public VistaMiPerfil(Page page) {
        super(page);
    }


    public void navegarAMiPerfil() {
        page.navigate("http://localhost:8080/spring/miPerfil");
    }

//


    public String obtenerNombreUsuario() {
        return page.textContent("div.estructura h2.palabra:nth-of-type(1)");
    }

    public String obtenerCarreraUsuario() {
        // ejemplo: "Profesorado en Matemática"
        // El HTML usa 'ul li.titulo' dentro de 'contenedor_usuario'.
        return page.textContent(".contenedor_usuario ul li.titulo");
    }

    // --- Publicaciones del usuario ---

    /**
     * Verifica que el contenedor principal de publicaciones esté presente y visible.
     */
    public boolean existeContenedorPublicaciones() {
        // el ID 'contenedor-publicaciones'
        return page.isVisible("#contenedor-publicaciones");
    }

    public boolean hayPublicaciones() {

        return page.locator("#contenedor-publicaciones > div").count() > 0;
    }

    public String obtenerTextoPrimeraPublicacion() {

        return hayPublicaciones() ? page.textContent("#contenedor-publicaciones > div:first-child p") : "";
    }

    public String obtenerLikesPrimeraPublicacion() {
        // Se asume que los likes están en un span dentro del primer DIV de publicación.
        return hayPublicaciones() ? page.textContent("#contenedor-publicaciones > div:first-child .like-count") : "0";
    }

    public String obtenerTextoBotonComentariosPrimeraPublicacion() {
        // Se asume que el botón de comentarios tiene un selector específico.
        return page.textContent("#contenedor-publicaciones > div:first-child .boton-comentarios");
    }

    public String obtenerTitulo() {
        String nombre = page.textContent("div.estructura h2.palabra:nth-of-type(1)");
        String apellido = page.textContent("div.estructura h2.palabra:nth-of-type(2)");
        return (nombre != null ? nombre : "") + " " + (apellido != null ? apellido : "");
    }
    public void darClickEnMiPerfil() {
        // 1. Clic en el título del menú para desplegar el submenú (selector: el enlace dentro del li.menu-perfil)
        page.click("li.menu-perfil a.menu-titulo");

        //  Clic en el enlace "Ver mi perfil" dentro del submenú
        // href exacto para mayor seguridad.
        page.click("li.menu-perfil ul.submenu a[href='/spring/miPerfil']");
    }
}
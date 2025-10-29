package com.tallerwebi.punta_a_punta.vistas;

import com.microsoft.playwright.Page;

public class VistaMiPerfil extends VistaWeb {

    public VistaMiPerfil(Page page) {
        super(page);
    }


    public void navegarAMiPerfil() {
        page.navigate("http://localhost:8080/spring/miPerfil");
    }
    // --- Navegación ---
    public void navegarAEditarInformacion() {
        page.click("a[href='/spring/editar-informacion']");
    }

    public void navegarAMisLikes() {
        page.click("a[href='/spring/misLikes']");
    }

    // --- Datos del usuario (panel izquierdo) ---
    public String obtenerNombreUsuario() {
        // ejemplo: "Admin Unlam"
        return page.textContent("div.profile-card h2");
    }

    public String obtenerCarreraUsuario() {
        // ejemplo: "Profesorado en Matemática"
        return page.textContent("div.profile-card li");
    }

    // --- Publicaciones del usuario ---
    public boolean hayPublicaciones() {
        return !page.querySelectorAll("article.estructura").isEmpty();
    }

    public String obtenerTextoPrimeraPublicacion() {
        // obtiene el texto <p class="texto">holi</p>
        return page.textContent("article.estructura p.texto");
    }

    public String obtenerLikesPrimeraPublicacion() {
        return page.textContent("article.estructura .like-container span");
    }

    public String obtenerTextoBotonComentariosPrimeraPublicacion() {
        return page.textContent("article.estructura .ver-comentariosBtn");
    }

    public String obtenerTitulo() {
        // suponiendo que hay un <h1> o <h2> en la vista del perfil
        return page.textContent("h1, h2");
    }
}

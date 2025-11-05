package com.tallerwebi.dominio;

import java.util.List;

public class InteresesGeneradosDTO {


        private String temaPrincipal;
        private List<String> tagsIntereses;
        private String resumenPerfil;

        // ➡️ Constructor por defecto necesario para Jackson
        public InteresesGeneradosDTO() {}

        // Getters y Setters (Necesarios para el mapeo de Jackson)
        public String getTemaPrincipal() {
            return temaPrincipal;
        }

        public void setTemaPrincipal(String temaPrincipal) {
            this.temaPrincipal = temaPrincipal;
        }

        public List<String> getTagsIntereses() {
            return tagsIntereses;
        }

        public void setTagsIntereses(List<String> tagsIntereses) {
            this.tagsIntereses = tagsIntereses;
        }

    public String getResumenPerfil() {
        return resumenPerfil;
    }

    public void setResumenPerfil(String resumenPerfil) {
        this.resumenPerfil = resumenPerfil;
    }
}

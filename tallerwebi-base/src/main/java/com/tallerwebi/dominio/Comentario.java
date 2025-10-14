package com.tallerwebi.dominio;

import javax.persistence.*;

    @Entity
    public class Comentario {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private long id;

        @Column(length = 200, nullable = false)
        private String texto;

        @ManyToOne(fetch = FetchType.EAGER, optional = false)
        @JoinColumn(name = "usuario_id", nullable = false)
        private Usuario usuario;

        @ManyToOne(fetch = FetchType.EAGER, optional = false)
        @JoinColumn(name = "publicacion_id", nullable = false)
        private Publicacion publicacion;

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getTexto() {
            return texto;
        }

        public void setTexto(String texto) {
            this.texto = texto;
        }

        public Usuario getUsuario() {
            return usuario;
        }

        public void setUsuario(Usuario usuario) {
            this.usuario = usuario;
        }

        public Publicacion getPublicacion() {
            return publicacion;
        }

        public void setPublicacion(Publicacion publicacion) {
            this.publicacion = publicacion;
        }
    }

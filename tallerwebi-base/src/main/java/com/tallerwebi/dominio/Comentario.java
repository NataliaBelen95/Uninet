package com.tallerwebi.dominio;

import javax.persistence.*;

    @Entity
    public class Comentario {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private long id;

        @Column(length = 200, nullable = false)
        private String texto;

        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "usuario_id", nullable = false)
        private Usuario usuario;

        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "publicacion_id", nullable = false)
        private Publicacion publicacion;
}

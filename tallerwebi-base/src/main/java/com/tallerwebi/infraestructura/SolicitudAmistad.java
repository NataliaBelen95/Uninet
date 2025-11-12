package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.EstadoSolicitud;
import com.tallerwebi.dominio.Usuario;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import java.time.LocalDateTime;

@Entity
public class SolicitudAmistad {

        @ManyToOne
        private Usuario solicitante;

        @ManyToOne
        private Usuario receptor;

        @Enumerated(EnumType.STRING)
        private EstadoSolicitud estado; // Debe ser el único que tiene el estado PENDIENTE/ACEPTADA/RECHAZADA

        private LocalDateTime fechaSolicitud;

}

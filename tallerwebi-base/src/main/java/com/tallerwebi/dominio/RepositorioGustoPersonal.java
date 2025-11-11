package com.tallerwebi.dominio;

import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepositorioGustoPersonal {
    void guardarOActualizar(GustosPersonal gustos);

    // Método para encontrar los gustos de un usuario específico.
    GustosPersonal buscarPorUsuario(Usuario usuario);

    List<Long> obtenerUsuariosAnalizadosId();
}

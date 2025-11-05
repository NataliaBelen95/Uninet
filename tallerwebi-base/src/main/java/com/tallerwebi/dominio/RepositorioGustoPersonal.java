package com.tallerwebi.dominio;

import org.springframework.stereotype.Repository;

@Repository
public interface RepositorioGustoPersonal {
    void guardarOActualizar(GustosPersonal gustos);

    // Método para encontrar los gustos de un usuario específico.
    GustosPersonal buscarPorUsuario(Usuario usuario);

}

// Archivo: com.tallerwebi.infraestructura.ServicioGustoPersonalImpl.java

package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.GustosPersonal;
import com.tallerwebi.dominio.RepositorioGustoPersonal;
import com.tallerwebi.dominio.ServicioGustoPersonal;
import com.tallerwebi.dominio.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service("servicioGustoPersonal")
@Transactional // ⬅️ ¡La clave para gestionar la sesión de Hibernate!
public class ServicioGustoPersonalImpl implements ServicioGustoPersonal {

    private final RepositorioGustoPersonal repositorioGustoPersonal;

    @Autowired
    public ServicioGustoPersonalImpl(RepositorioGustoPersonal repositorioGustoPersonal) {
        this.repositorioGustoPersonal = repositorioGustoPersonal;
    }

    @Override
    public void guardarOActualizar(GustosPersonal gustos) {
        // La transacción se inicia aquí. El repositorio ejecuta la acción de guardar/actualizar.
        repositorioGustoPersonal.guardarOActualizar(gustos);
    }

    @Override
    @Transactional(readOnly = true) // Solo lectura
    public GustosPersonal buscarPorUsuario(Usuario usuario) {
        return repositorioGustoPersonal.buscarPorUsuario(usuario);
    }

}
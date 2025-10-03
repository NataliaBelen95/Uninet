package com.tallerwebi.integracion;
import com.tallerwebi.dominio.*;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import com.tallerwebi.infraestructura.config.HibernateTestInfraesructuraConfig;
import com.tallerwebi.integracion.config.HibernateTestConfig;
import com.tallerwebi.integracion.config.SpringWebTestConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;


@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {HibernateTestInfraesructuraConfig.class})
public class ServicioLoguinTest {
    @Autowired
    private ServicioLogin servicioLogin;


    @Test
    public void deberiaRegistrarUnUsuarioYagregarloAUsuarios() throws UsuarioExistente {

        Usuario usuario = new Usuario();
        usuario.setNombre("Juan") ;
        usuario.setApellido("Sanchez") ;
        usuario.setEmail("sj@unlam") ;
        usuario.setDni(40555782);
        Carrera c1 = new Carrera();
        c1.setNombre("Tecnicatura en Desarrollo Web") ;
        Materia m1 = new Materia();
        m1.setNombre("Diseño gráfico en la web") ;
        Materia m2 = new Materia();
        m2.setNombre("Inglés Técnico 1") ;
        List<Materia> materiasTec = new ArrayList<Materia>();
        c1.setMaterias(materiasTec);
        usuario.setCarrera(c1);


        this.servicioLogin.registrar(usuario) ;


    }

}

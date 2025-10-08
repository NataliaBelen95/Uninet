package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Comentario;
import com.tallerwebi.dominio.RepositorioComentario;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.excepcion.ErrorEnEditarComentario;
import com.tallerwebi.dominio.excepcion.NoSePudoComentar;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;


@Repository
public class RepositorioComentarioImpl implements RepositorioComentario {

    @Autowired
    private SessionFactory sessionFactory;


    @Override
    public void guardar(Comentario comentario) {

        sessionFactory.getCurrentSession().save(comentario);


    }



    @Override
    public void eliminar(long id) {
        sessionFactory.getCurrentSession().delete(id);

    }

    @Override
    public Comentario buscar(long id) {
        return sessionFactory.getCurrentSession().get(Comentario.class, id);
    }
}

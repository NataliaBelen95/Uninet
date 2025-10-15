package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Comentario;
import com.tallerwebi.dominio.Publicacion;
import com.tallerwebi.dominio.RepositorioComentario;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.excepcion.ErrorEnEditarComentario;
import com.tallerwebi.dominio.excepcion.NoSePudoComentar;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public class RepositorioComentarioImpl implements RepositorioComentario {

    @Autowired
    private SessionFactory sessionFactory;


    @Override
    public Comentario guardar(Comentario comentario) {
        sessionFactory.getCurrentSession().save(comentario);
        return comentario;
    }



    @Override
    public void eliminar(Comentario comentario) {
        sessionFactory.getCurrentSession().delete(comentario);

    }

    @Override
    public Comentario buscar(long id) {
        return sessionFactory.getCurrentSession().get(Comentario.class, id);
    }

    @Override
    public int contarComentarioPorPublicacion(long idPubli) {

        String hql = "SELECT COUNT(c) FROM Comentario c WHERE c.publicacion.id = :publicacionId";
        Long count = (Long) sessionFactory.getCurrentSession()
                .createQuery(hql)
                .setParameter("publicacionId", idPubli)
                .uniqueResult();

        return (count != null) ? count.intValue() : 0; // Devolver 0 si count es null
    }

    @Override
    public List<Comentario> findComentariosByPublicacionId(long publicacionId) {
        return sessionFactory.getCurrentSession()
                .createQuery("SELECT c FROM Comentario c " +
                        "WHERE c.publicacion.id = :id", Comentario.class)
                .setParameter("id", publicacionId)
                .getResultList();
    }


}




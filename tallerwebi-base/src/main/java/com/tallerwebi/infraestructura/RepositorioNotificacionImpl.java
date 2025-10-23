package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.*;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Repository
@Transactional
public class RepositorioNotificacionImpl implements RepositorioNotificacion {

     private SessionFactory sessionFactory;


    @Autowired
    public RepositorioNotificacionImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;}



    @Override
    public void guardar(Notificacion notificacion) {
        sessionFactory.getCurrentSession().save(notificacion);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Notificacion> buscarPorReceptor(Long receptorId) {
        return sessionFactory.getCurrentSession()
                .createQuery(
                        "SELECT n " +
                                "FROM Notificacion n " +
                                "LEFT JOIN FETCH n.publicacion " +
                                "LEFT JOIN FETCH n.usuarioEmisor " +  //agrego el join fetch para usuarioEmisor
                                "WHERE n.usuarioReceptor.id = :receptorId " +
                                "ORDER BY n.fechaCreacion DESC"
                )
                .setParameter("receptorId", receptorId)
                .list();
    }

    @Override
    public void marcarComoLeida(Long id) {
        Notificacion notificacion = sessionFactory.getCurrentSession().get(Notificacion.class, id);
        if (notificacion != null) {
            notificacion.setLeida(true);
            sessionFactory.getCurrentSession().update(notificacion);
        }
    }

    @Override
    public int contarPublisNoLeidasPorUsuario(long usuarioId) {
        Long cantidad = (Long) sessionFactory.getCurrentSession()
                .createQuery(
                        "SELECT COUNT(n) " +
                                "FROM Notificacion n " +
                                "WHERE n.usuarioReceptor.id = :usuarioId " +
                                "AND n.leida = false"
                )
                .setParameter("usuarioId", usuarioId)
                .uniqueResult();

        return cantidad != null ? cantidad.intValue() : 0;
    }
}

package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.ArchivoPublicacion;
import com.tallerwebi.dominio.Publicacion;
import com.tallerwebi.dominio.RepositorioPublicacionArchivo;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.hibernate.query.Query;


@Repository
public class RepositorioPublicacionArchivoImpl  implements RepositorioPublicacionArchivo {

    private final SessionFactory sessionFactory;
    @Autowired
    public RepositorioPublicacionArchivoImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;

    }

    @Override
    public ArchivoPublicacion findByPublicacion(Publicacion publicacion) {
        Session session = sessionFactory.getCurrentSession();
        Query<ArchivoPublicacion> query = session.createQuery("FROM ArchivoPublicacion a WHERE a.publicacion = :publicacion", ArchivoPublicacion.class);
        query.setParameter("publicacion", publicacion);
        return query.uniqueResult();
    }

    @Override
    public ArchivoPublicacion guardarArchivoPublicacion(ArchivoPublicacion archivoPublicacion) {
        Session session = sessionFactory.getCurrentSession();
        session.saveOrUpdate(archivoPublicacion);
        return archivoPublicacion;  // retorno la entidad que ya tiene el ID seteado (si es nueva)
    }


}

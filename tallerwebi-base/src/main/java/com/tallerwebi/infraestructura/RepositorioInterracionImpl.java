package com.tallerwebi.infraestructura;


import com.tallerwebi.dominio.Interaccion;
import com.tallerwebi.dominio.Publicacion;
import com.tallerwebi.dominio.RepositorioInteraccion;
import com.tallerwebi.dominio.Usuario;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class RepositorioInterracionImpl implements RepositorioInteraccion {

    private final SessionFactory sessionFactory;

    public RepositorioInterracionImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }


    @Override
    public void guardar(Interaccion interaccion) {
        sessionFactory.getCurrentSession().save(interaccion);
    }

    @Override
    public List<Interaccion> encontrarDeUsuario(Usuario usuario) {
        return sessionFactory.getCurrentSession()
                .createQuery("FROM Interaccion i WHERE i.usuario = :usuario", Interaccion.class) // <--- aquí
                .setParameter("usuario", usuario)
                .getResultList();
    }

    @Override
    public List<Interaccion> encontrarDePubli(Publicacion publicacion) {
        return sessionFactory.getCurrentSession()
                .createQuery("FROM Interaccion i WHERE i.publicacion = :publicacion", Interaccion.class)
                .setParameter("publicacion", publicacion)
                .getResultList();
    }

    @Override
    public List<Interaccion> encontrarDeUsuarioAndTipo(Usuario usuario, String tipo) {
        return sessionFactory.getCurrentSession()
                .createQuery("FROM Interaccion i WHERE i.usuario = :usuario AND i.tipo = :tipo", Interaccion.class)
                .setParameter("usuario", usuario)
                .setParameter("tipo", tipo)
                .getResultList();
    }

    @Override
    public List<Interaccion> encontrarPorUsuarioYPubli(Usuario usuario, Publicacion publicacion) {
        return sessionFactory.getCurrentSession()
                .createQuery("FROM Interaccion i WHERE i.usuario = :usuario AND i.publicacion = :publicacion", Interaccion.class)
                .setParameter("usuario", usuario)
                .setParameter("publicacion", publicacion)
                .getResultList();
    }

    @Override
    public void eliminar(long interaccionId) {
        Session session = sessionFactory.getCurrentSession();
        Interaccion interaccion = session.get(Interaccion.class, interaccionId);
        if (interaccion != null) {
            session.delete(interaccion); // 🔹 Esto marca la entidad para borrado
        }
    }
    @Override
    public String consolidarTextoInteraccionesRecientes(Usuario usuario, int limite) {
        // 1. Obtener la sesión actual de Hibernate
        Session session = sessionFactory.getCurrentSession();

        // 2. Consulta HQL:
        // - Filtra por el usuario
        // - Ordena por fecha (asumiendo que Interaccion tiene un campo 'fecha') de forma descendente (más recientes primero)
        // - Limita el número de resultados (las interacciones más recientes)
        List<Interaccion> interaccionesRecientes = session.createQuery(
                        "FROM Interaccion i WHERE i.usuario = :usuario ORDER BY i.fecha DESC", Interaccion.class)
                .setParameter("usuario", usuario)
                .setMaxResults(limite) // Limita a las N interacciones más recientes
                .getResultList();

        // 3. Consolidar el texto
        StringBuilder textoConsolidado = new StringBuilder();
        for (Interaccion i : interaccionesRecientes) {

            if (i.getContenido() != null && !i.getContenido().isEmpty()) {
                textoConsolidado.append(i.getContenido()).append(". ");
            }

        }

        return textoConsolidado.toString().trim();
    }

}

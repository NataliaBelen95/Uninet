package com.tallerwebi.infraestructura;
// ... (imports) ...

import com.tallerwebi.dominio.Amistad;
import com.tallerwebi.dominio.RepositorioAmistad;
import com.tallerwebi.dominio.Usuario;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Repository
public class RepositorioAmistadImpl implements RepositorioAmistad {

    private final SessionFactory sessionFactory;

    @Autowired
    public RepositorioAmistadImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void guardar(Amistad amistad) {
        sessionFactory.getCurrentSession().save(amistad);
    }

    @Override
    public void actualizar(Amistad amistad) {
        sessionFactory.getCurrentSession().update(amistad);
    }

    // --- MÉTODOS DE AMISTAD FINAL ---

    @Override
    public boolean sonAmigos(Usuario u1, Usuario u2) {
        // ✅ CORRECCIÓN DE ESTILO Y LÓGICA: Busca si existe una fila A-B o B-A en la tabla Amistad
        Session session = sessionFactory.getCurrentSession();
        String hql = "FROM Amistad a WHERE " +
                " (a.solicitante = :usuario1 AND a.solicitado = :usuario2) " +
                " OR " +
                " (a.solicitante = :usuario2 AND a.solicitado = :usuario1)";

        Query<Amistad> query = session.createQuery(hql, Amistad.class);
        query.setParameter("usuario1", u1);
        query.setParameter("usuario2", u2);
        query.setMaxResults(1); // Optimización

        // Si la lista no está vacía, son amigos.
        return !query.list().isEmpty();
    }

    @Override
    public Amistad buscarPorUsuarios(Usuario solicitante, Usuario solicitado) {
        Session session = sessionFactory.getCurrentSession();
        Query<Amistad> query = session.createQuery(
                "FROM Amistad WHERE (solicitante = :solicitante AND solicitado = :solicitado) OR (solicitante = :solicitado AND solicitado = :solicitante)",
                Amistad.class);
        query.setParameter("solicitante", solicitante);
        query.setParameter("solicitado", solicitado);
        return query.uniqueResult();
    }

    @Override
    public List<Usuario> obtenerAmigosDeUsuario(long idUsuario) {
        final Session session = sessionFactory.getCurrentSession();

        // 1. Consulta A: Buscar usuarios donde el usuario actual es el solicitante.
        // (Obtenemos el 'solicitado' donde 'solicitante.id' coincide con el ID del usuario actual)
        String hqlSolicitante = "SELECT a.solicitado FROM Amistad a WHERE a.solicitante.id = :idUsuario";
        List<Usuario> amigosComoSolicitante = session.createQuery(hqlSolicitante, Usuario.class)
                .setParameter("idUsuario", idUsuario)
                .getResultList();

        // 2. Consulta B: Buscar usuarios donde el usuario actual es el solicitado.
        // (Obtenemos el 'solicitante' donde 'solicitado.id' coincide con el ID del usuario actual)
        String hqlSolicitado = "SELECT a.solicitante FROM Amistad a WHERE a.solicitado.id = :idUsuario";
        List<Usuario> amigosComoSolicitado = session.createQuery(hqlSolicitado, Usuario.class)
                .setParameter("idUsuario", idUsuario)
                .getResultList();

        // 3. Unir y Eliminar Duplicados (LinkedHashSet mantiene el orden y asegura unicidad)
        Set<Usuario> amigosUnidos = new LinkedHashSet<>();
        if (amigosComoSolicitante != null) amigosUnidos.addAll(amigosComoSolicitante);
        if (amigosComoSolicitado != null) amigosUnidos.addAll(amigosComoSolicitado);

        // Devolver la lista final de amigos.
        return new ArrayList<>(amigosUnidos);
    }

    @Override
    public List<Amistad> obtenerAmistadesAceptadasDe(long usuarioId) {
        return sessionFactory.getCurrentSession()
                .createQuery("FROM Amistad a WHERE (a.solicitante.id = :id OR a.solicitado.id = :id)", Amistad.class)
                .setParameter("id", usuarioId)
                .getResultList();
    }


    @Override
    public boolean existeAmistadAceptadaEntre(long usuarioAId, long usuarioBId) {
        Long count = sessionFactory.getCurrentSession()
                .createQuery(
                        "SELECT count(a) FROM Amistad a WHERE ((a.solicitante.id = :a AND a.solicitado.id = :b) OR (a.solicitante.id = :b AND a.solicitado.id = :a))",
                        Long.class)
                .setParameter("a", usuarioAId)
                .setParameter("b", usuarioBId)
                .uniqueResult();
        return count != null && count > 0L;
    }


}
package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.GustosPersonal;
import com.tallerwebi.dominio.RepositorioGustoPersonal;
import com.tallerwebi.dominio.Usuario;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public class RepositorioGustoPersonalImpl implements RepositorioGustoPersonal {

    private final SessionFactory sessionFactory;

    public RepositorioGustoPersonalImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void guardarOActualizar(GustosPersonal gustos) {
        // saveOrUpdate permite guardar una nueva entidad o actualizar una existente
        // si ya tiene un ID
        sessionFactory.getCurrentSession().merge(gustos);
    }

    @Override
    public GustosPersonal buscarPorUsuario(Usuario usuario) {
        Session session = sessionFactory.getCurrentSession();
        return session.createQuery("FROM GustosPersonal g WHERE g.usuario.id = :idUsuario", GustosPersonal.class)
                .setParameter("idUsuario", usuario.getId())
                .uniqueResult();
    }
//para saber a que usaurios publicarles
@Override
public List<Long> obtenerUsuariosAnalizadosId() {
    Session session = sessionFactory.getCurrentSession();

    return session.createQuery(
                    "SELECT DISTINCT g.usuario.id FROM GustosPersonal g", Long.class)
            .getResultList();
}

}

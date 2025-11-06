package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.ChatMessage;
import com.tallerwebi.dominio.RepositorioChat;
import org.hibernate.SessionFactory;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class RepositorioChatImpl implements RepositorioChat {

    private final SessionFactory sessionFactory;

    @Autowired
    public RepositorioChatImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public ChatMessage guardar(ChatMessage mensaje) {
        Session session = sessionFactory.getCurrentSession();
        try {
            session.saveOrUpdate(mensaje);
            session.flush();
            System.out.println("RepositorioChatImpl: guardado mensaje id=" + mensaje.getId());
            return mensaje;
        } catch (Exception e) {
            System.err.println("RepositorioChatImpl: error guardando mensaje: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ChatMessage> obtenerConversacion(Long userA, Long userB) {
        return sessionFactory.getCurrentSession()
                .createQuery("FROM ChatMessage m WHERE " +
                        "(m.fromUserId = :a AND m.toUserId = :b) OR (m.fromUserId = :b AND m.toUserId = :a) " +
                        "ORDER BY m.timestamp ASC", ChatMessage.class)
                .setParameter("a", userA)
                .setParameter("b", userB)
                .list();
    }
}
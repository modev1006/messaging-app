package dao;

import model.Message;
import model.MessageStatus;
import model.User;
import org.hibernate.Session;
import org.hibernate.Transaction;
import util.HibernateUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageDAO {

    public void save(Message message) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            if (message.getId() == null) {
                session.persist(message);
            } else {
                session.merge(message);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        }
    }

    public List<Message> getHistory(User u1, User u2) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "from Message where (sender = :u1 and receiver = :u2) or (sender = :u2 and receiver = :u1) order by dateEnvoi",
                    Message.class)
                    .setParameter("u1", u1)
                    .setParameter("u2", u2)
                    .list();
        }
    }

    public List<Message> getPendingMessages(User user) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "from Message where receiver = :user and statut = :statut order by dateEnvoi",
                    Message.class)
                    .setParameter("user", user)
                    .setParameter("statut", MessageStatus.ENVOYE)
                    .list();
        }
    }

    public List<Integer> markAsRead(User sender, User receiver) {
        List<Integer> updatedIds = new ArrayList<>();
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Récupérer les messages non lus
            List<Message> unread = session.createQuery(
                    "from Message where sender = :sender and receiver = :receiver and statut != :lu",
                    Message.class)
                    .setParameter("sender", sender)
                    .setParameter("receiver", receiver)
                    .setParameter("lu", MessageStatus.LU)
                    .list();

            transaction = session.beginTransaction();
            for (Message m : unread) {
                updatedIds.add(m.getId().intValue());
                m.setStatut(MessageStatus.LU);
                session.merge(m);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        }
        return updatedIds;
    }

    public Map<String, Long> getUnreadCounts(User user) {
        Map<String, Long> counts = new HashMap<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Object[]> results = session.createQuery(
                    "select m.sender.username, count(m) from Message m where m.receiver = :user and m.statut != :lu group by m.sender.username",
                    Object[].class)
                    .setParameter("user", user)
                    .setParameter("lu", MessageStatus.LU)
                    .list();
            for (Object[] row : results) {
                counts.put((String) row[0], (Long) row[1]);
            }
        }
        return counts;
    }

    // Anciennes méthodes conservées pour compatibilité
    public void saveMessage(Message message) { save(message); }
    public List<Message> getMessagesBetweenUsers(User u1, User u2) { return getHistory(u1, u2); }
}

package dao;

import model.Message;
import model.User;
import org.hibernate.Session;
import org.hibernate.Transaction;
import util.HibernateUtil;
import java.util.List;

public class MessageDAO {
    public void saveMessage(Message message) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(message);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        }
    }

    public List<Message> getMessagesBetweenUsers(User u1, User u2) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("from Message where (sender = :u1 and receiver = :u2) or (sender = :u2 and receiver = :u1) order by timestamp", Message.class)
                    .setParameter("u1", u1)
                    .setParameter("u2", u2)
                    .list();
        }
    }
}

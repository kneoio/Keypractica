package com.semantyca.repository;

import com.semantyca.model.user.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.neo4j.ogm.exception.CypherException;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.transaction.Transaction;

import java.time.LocalDateTime;
import java.util.*;

@ApplicationScoped
public class UserRepository {

    @Inject
    SessionFactory sessionFactory;

    public List<User> getAllUsers (int i, int i1) {
        Session session = sessionFactory.openSession();
        Iterable<User> iterable = session.query(User.class, "MATCH (u:User)-[:DEFAULT_LANG]->(l:User) RETURN u, l", Map.of());
        return resultList(iterable);
    }

    private static <User> List<User> resultList(Iterable<User> result) {
        ArrayList<User> list = new ArrayList<>();
        result.forEach(list::add);
        return list;
    }

    public User findById(UUID uuid) {
        Session session = sessionFactory.openSession();
        String query = "MATCH (u:User {id: $id})-[:DEFAULT_LANG]->(l:User) RETURN u, l";
        Iterable<User> users = session.query(User.class, query, Collections.singletonMap("id", uuid.toString()));
        if (users.iterator().hasNext()) {
            return users.iterator().next();
        }
        return null;
    }

    public Optional<User> findByValue(String base) {
        return null;
    }

    public Long insert(User node, Long user) {
        Session session = sessionFactory.openSession();
        try {
            session.beginTransaction();
            node.setIdentifier(10L);
            LocalDateTime nowTime = LocalDateTime.now();
            node.setRegDate(nowTime);
            node.setLastModifiedDate(nowTime);
            node.setAuthor(user);
            node.setLastModifier(user);
            session.save(node);
            session.getTransaction().commit();
        } catch (CypherException e) {
            System.out.println(e.getMessage());
            return null;
        } catch (Exception e) {
            if(session.getTransaction().status() == Transaction.Status.OPEN) {
                session.getTransaction().rollback();
            }
            return null;
        } finally {
            session.clear();
        }
        return node.getIdentifier();
    }


    public User update(User user) {
        Session session = sessionFactory.openSession();
        session.beginTransaction();
        session.save(user);
        session.getTransaction().commit();
        return user;
    }

    public int delete(Long id) {
        Session session = sessionFactory.openSession();
        session.beginTransaction();
        session.delete(id);
        session.getTransaction().commit();
        return 1;
    }
}

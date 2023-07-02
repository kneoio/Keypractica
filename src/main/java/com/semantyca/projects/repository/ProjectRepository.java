package com.semantyca.projects.repository;

import com.semantyca.model.Language;
import com.semantyca.projects.model.Project;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.neo4j.ogm.exception.CypherException;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.transaction.Transaction;

import java.time.LocalDateTime;
import java.util.*;

@ApplicationScoped
public class ProjectRepository {

    @Inject
    SessionFactory sessionFactory;

    public List<Project> getAll(int i, int i1) {
        Session session = sessionFactory.openSession();
        Iterable<Project> iterable = session.query(Project.class, "MATCH (l:Project) RETURN l", Map.of());
        return resultList(iterable);
    }

    private static <Project> List<Project> resultList(Iterable<Project> result) {
        ArrayList<Project> list = new ArrayList<>();
        result.forEach(list::add);
        return list;
    }

    public Project findById(UUID uuid) {
        Session session = sessionFactory.openSession();
        String query = "MATCH (u:User {id: $id})-[:DEFAULT_LANG]->(l:Language) RETURN u, l";
        Iterable<Project> users = session.query(Project.class, query, Collections.singletonMap("id", uuid.toString()));
        if (users.iterator().hasNext()) {
            return users.iterator().next();
        }
        return null;
    }

    public Optional<Project> findByValue(String base) {
        return null;
    }

    public String insert(Project node, Long user) {
        Session session = sessionFactory.openSession();
        try {
            session.beginTransaction();
            node.setIdentifier(UUID.randomUUID().toString());
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


    public Language update(Language node) {
        Session session = sessionFactory.openSession();
        session.beginTransaction();
        session.save(node);
        session.getTransaction().commit();
        return node;
    }

    public int delete(Long id) {
        Session session = sessionFactory.openSession();
        session.beginTransaction();
        session.delete(id);
        session.getTransaction().commit();
        return 1;
    }
}

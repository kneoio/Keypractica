package com.semantyca.projects.repository;

import com.semantyca.model.Language;
import com.semantyca.projects.model.Project;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class ProjectRepository {


    public List<Project> getAll(int i, int i1) {
        return null;
    }

    private static <Project> List<Project> resultList(Iterable<Project> result) {
        ArrayList<Project> list = new ArrayList<>();
        result.forEach(list::add);
        return list;
    }

    public Project findById(UUID uuid) {
        return null;
    }

    public Optional<Project> findByValue(String base) {
        return null;
    }

    public String insert(Project node, Long user) {

        return node.getIdentifier();
    }


    public Language update(Language node) {

        return node;
    }

    public int delete(Long id) {

        return 1;
    }
}

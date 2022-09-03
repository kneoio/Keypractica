package com.semantyca.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.semantyca.localization.LanguageCode;
import com.semantyca.repository.glossary.Label;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.generic.GenericType;
import org.jdbi.v3.core.qualifier.QualifiedType;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class LabelRepository extends AbstractRepository {

    private Jdbi jdbi;

    @Inject
    public LabelRepository(Jdbi jdbi) {
        this.jdbi = jdbi;

    }

    public Optional<Label> findById(UUID id) {
        return jdbi.withHandle(handle ->
                handle.createQuery("SELECT * FROM labels a WHERE a.id = '" + id + "'")
                        .map(new LabelMapper()).findFirst());
    }

    public Optional<Label> findByValue(String word, boolean includeAssociated) {
        return jdbi.withHandle(handle ->
                handle.createQuery("SELECT * FROM labels a WHERE a.value = '" + word + "'")
                        .map(new LabelMapper()).findFirst());
    }

    public List<Label> findAll(final int limit, final int offset) {
        String sql = "SELECT * FROM labels LIMIT " + limit + " OFFSET " + offset;
        if (limit == 0 && offset == 0) {
            sql = "SELECT * FROM labels";
        }
        String finalSql = sql;
        return jdbi.withHandle(handle ->
                handle.createQuery(finalSql)
                        .map(new LabelMapper()).list());
    }


    @Transactional
    public Label insert(Label entity, int user) throws JsonProcessingException {
        QualifiedType<Map<LanguageCode, String>> json = QualifiedType.of(new GenericType<>() {});

        return jdbi.withHandle(handle -> {
            handle.registerArgument(new LocalizedNamesArgumentFactory());
            Label label = handle.createUpdate("INSERT INTO labels (reg_date, title, author, last_mod_date, last_mod_user, name, rank, is_active, localized_names, category, color)" +
                    "VALUES (:regDate, :title, :author, :lastModifiedDate, :lastModifier, :name, :rank, :active,  :localizedNames, :category, :color)")
                    .bind("regDate", ZonedDateTime.now())
                    .bind("title", entity.getTitle())
                    .bind("author", user)
                    .bind("lastModifiedDate", ZonedDateTime.now())
                    .bind("lastModifier", user)
                    .bind("name", entity.getName())
                    .bind("rank", entity.getRank())
                    .bind("active", entity.isActive())
                    .bindByType("localizedNames", entity.getLocalizedNames(), json)
                    .bind("category", entity.getCategory())
                    .bind("color", entity.getColor())
           //         .bindBean(entity)
                    .executeAndReturnGeneratedKeys()
                    .map(new LabelMapper())
                    .one();

            return label;
        });
    }

    @Transactional
    public Label update(Label entity, int user)  {
        return jdbi.withHandle(handle -> {
           Label label = handle.createUpdate("UPDATE label " +
                    "SET title=:title, last_mod_date=:lastModifiedDate, last_mod_user=:lastModifier, name=:name, rank=:rank, active=:active, localized_names =to_jsonb(:localizedNames), category=:category, color=:color" +
                    "WHERE id=:id")
                    .bind("title", entity.getTitle())
                    .bind("lastModifiedDate", ZonedDateTime.now())
                    .bind("lastModifier", user)
                    .bind("name", entity.getName())
                    .bind("rank", entity.getRank())
                    .bind("active", entity.isActive())
                    .bind("localizedNames", "{}")
                    .bind("category", entity.getCategory())
                    .bind("color", entity.getColor())
                    .executeAndReturnGeneratedKeys()
                    .map(new LabelMapper())
                    .one();
            return label;
        });
    }

    //TODO It needs access check
    public int delete(Label label, int user) {
        jdbi.useHandle(handle -> {
            handle.createUpdate("DELETE FROM word_emphasis_rank_links WHERE related_word_id = :id")
                    .bind("id", label.getId())
                    .execute();
            handle.createUpdate("DELETE FROM word_emphasis_rank_links WHERE primary_word_id = :id")
                    .bind("id", label.getId())
                    .execute();
            handle.createUpdate("DELETE FROM word_formality_rank_links WHERE related_word_id = :id")
                    .bind("id", label.getId())
                    .execute();
            handle.createUpdate("DELETE FROM word_formality_rank_links WHERE primary_word_id = :id")
                    .bind("id", label.getId())
                    .execute();
            handle.createUpdate("DELETE FROM label WHERE id = :id")
                    .bind("id", label.getId())
                    .execute();
        });
//        resetCache();
        return 1;


    }
}

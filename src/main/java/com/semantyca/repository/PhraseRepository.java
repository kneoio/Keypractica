package com.semantyca.repository;

import com.semantyca.model.embedded.RLSEntry;
import com.semantyca.model.phrase.Phrase;
import com.semantyca.repository.exception.DocumentModificationAccessException;
import com.semantyca.repository.glossary.Label;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.semantyca.repository.AbstractRepository.EDIT_AND_DELETE_ARE_ALLOWED;
import static com.semantyca.repository.AbstractRepository.EDIT_IS_ALLOWED;

public class PhraseRepository {
    private Jdbi jdbi;
    private LabelRepository labelRepository;

    @Inject
    public PhraseRepository(Jdbi jdbi) {
        this.jdbi = jdbi;
        labelRepository = new LabelRepository(jdbi);
    }

    public Optional<Phrase> findByValue(String sentence) {
        return jdbi.withHandle(handle ->
                handle.createQuery("SELECT * FROM phrases a WHERE a.base LIKE '" + sentence + "%'")
                        .map(new PhraseMapper()).findFirst());
    }

    public List<Phrase> findAll(final int limit, final int offset) {
        String sql = "SELECT * FROM phrases LIMIT " + limit + " OFFSET " + offset;
        if (limit == 0 && offset == 0) {
            sql = "SELECT * FROM phrases";
        }
        String finalSql = sql;
        return jdbi.withHandle(handle ->
                handle.createQuery(finalSql)
                        .map(new PhraseMapper()).list());
    }

    public Optional<Phrase> findById(UUID id) {
        return jdbi.withHandle(handle ->
                handle.createQuery("SELECT * FROM phrases a WHERE a.id = '" + id + "'")
                        .map(new PhraseMapper()).findFirst());
    }

    @Transactional
    public Phrase insert(Phrase entity) {
        return jdbi.withHandle(handle -> {
            Phrase sentence = handle.createUpdate("INSERT INTO phrases (reg_date, title, author, last_mod_date, last_mod_user, base, translation, base_pronunciation, translation_pronunciation)" +
                            "VALUES (:regDate, :title, :author, :lastModifiedDate, :lastModifier, :base, :translation, :basePronunciation, :translationPronunciation )")
                    .bindBean(entity)
                    .executeAndReturnGeneratedKeys()
                    .map(new PhraseMapper())
                    .one();
            for (RLSEntry rlsEntry : entity.getReaders()) {
                if (entity.getAuthor() == rlsEntry.getReader()) {
                    addReader(sentence.getId(), rlsEntry.getReader(), ZonedDateTime.now(), EDIT_AND_DELETE_ARE_ALLOWED);
                } else {
                    addReader(sentence.getId(), rlsEntry.getReader(), null, rlsEntry.getAccessLevel());
                }
            }
            return sentence;
        });
    }

    @Transactional
    public Phrase update(Phrase entity, int user) throws DocumentModificationAccessException {
        int accessLevel = jdbi.withHandle(handle -> handle.select("SELECT is_edit_allowed FROM phrase_rls wr WHERE wr.entity_id = :id AND wr.reader = :reader")
                .bind("id", entity.getId())
                .bind("reader", user)
                .mapTo(int.class).findOne().orElse(0));

        if (accessLevel >= EDIT_IS_ALLOWED) {
            return jdbi.withHandle(handle -> {
                Phrase word = handle.createUpdate("UPDATE phrases " +
                                "SET title=:title, last_mod_date=:lastModifiedDate, last_mod_user=:lastModifier, base=:base, translation=:translation, base_pronunciation=:basePronunciation, translation_pronunciation =:translationPronunciation " +
                                "WHERE id=:id")
                        .bindBean(entity)
                        .executeAndReturnGeneratedKeys()
                        .map(new PhraseMapper())
                        .one();
                handle.createUpdate("DELETE FROM phrase_labels WHERE entity_id = :id")
                        .bind("id", word.getId())
                        .execute();

                updateLabels(handle, word);
                return word;
            });
        } else {
            throw new DocumentModificationAccessException(entity.getId());
        }
    }

    public int delete(Phrase phrase) {
        jdbi.useHandle(handle -> {
            handle.createUpdate("DELETE FROM phrases WHERE id = :id")
                    .bind("id", phrase.getId())
                    .execute();
        });
//        resetCache();
        return 1;
    }
    int addReader(UUID id, int user, ZonedDateTime readingTime, int editAllowed) {
        return jdbi.withHandle(handle -> {
            int result = handle.createUpdate("INSERT INTO phrase_rls (entity_id, reader, reading_time, is_edit_allowed) " +
                            "VALUES (:id, :user, :readingTime, :editAllowed)")
                    .bind("id", id)
                    .bind("user", user)
                    .bind("readingTime", readingTime)
                    .bind("editAllowed", editAllowed)
                    .execute();
            return result;
        });
    }

    private boolean updateLabels(Handle handle, Phrase entity) {
        for (Label label : entity.getLabels()) {
            Optional<Label> optionalLabel = labelRepository.findById(label.getId());
            if (optionalLabel.isPresent()) {
                handle.createUpdate("INSERT INTO phrase_labels (entity_id, label_id)" +
                                "VALUES (:wordId, :labelId)")
                        .bind("wordId", entity.getId())
                        .bind("labelId", optionalLabel.get().getId())
                        .execute();
            }
        }
        return true;
    }

}

package com.semantyca.repository;

public class PhraseRepository {
 /*   private Jdbi jdbi;
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
            Phrase phrase = handle.createUpdate("INSERT INTO phrases (reg_date, title, author, last_mod_date, last_mod_user, base, translation, base_pronunciation, translation_pronunciation)" +
                            "VALUES (:regDate, :title, :author, :lastModifiedDate, :lastModifier, :base, :translation, :basePronunciation, :translationPronunciation )")
                    .bindBean(entity)
                    .executeAndReturnGeneratedKeys()
                    .map(new PhraseMapper())
                    .one();
            for (RLSEntry rlsEntry : entity.getReaders()) {
                if (entity.getAuthor() == rlsEntry.getReader()) {
                    addReader(phrase.getId(), rlsEntry.getReader(), ZonedDateTime.now(), EDIT_AND_DELETE_ARE_ALLOWED);
                } else {
                    addReader(phrase.getId(), rlsEntry.getReader(), null, rlsEntry.getAccessLevel());
                }
            }
            updateLabels(handle, phrase, entity.getLabels());
            return phrase;
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
                Phrase phrase = handle.createUpdate("UPDATE phrases " +
                                "SET title=:title, last_mod_date=:lastModifiedDate, last_mod_user=:lastModifier, base=:base, translation=:translation, base_pronunciation=:basePronunciation, translation_pronunciation =:translationPronunciation " +
                                "WHERE id=:id")
                        .bindBean(entity)
                        .executeAndReturnGeneratedKeys()
                        .map(new PhraseMapper())
                        .one();
                handle.createUpdate("DELETE FROM phrase_labels WHERE entity_id = :id")
                        .bind("id", phrase.getId())
                        .execute();

                updateLabels(handle, phrase);
                return phrase;
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
        return updateLabels(handle, entity, entity.getLabels());
    }

    private boolean updateLabels(Handle handle, Phrase entity, List<Label> labels) {
        for (Label label : labels) {
            Optional<Label> optionalLabel = labelRepository.findByValue(label.getName());
            if (optionalLabel.isPresent()) {
                handle.createUpdate("INSERT INTO phrase_labels (entity_id, label_id)" +
                                "VALUES (:entityId, :labelId)")
                        .bind("entityId", entity.getId())
                        .bind("labelId", optionalLabel.get().getId())
                        .execute();
            }
        }
        return true;
    }*/

}

package io.kneo.kneobroadcaster.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.kneo.core.model.user.IUser;
import io.kneo.core.repository.AsyncRepository;
import io.kneo.core.repository.exception.DocumentHasNotFoundException;
import io.kneo.core.repository.exception.DocumentModificationAccessException;
import io.kneo.core.repository.rls.RLSRepository;
import io.kneo.core.repository.table.EntityData;
import io.kneo.kneobroadcaster.model.SoundFragment;
import io.kneo.kneobroadcaster.model.cnst.FragmentType;
import io.kneo.kneobroadcaster.model.cnst.SourceType;
import io.kneo.kneobroadcaster.repository.table.KneoBroadcasterNameResolver;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.FileUpload;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Tuple;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static io.kneo.kneobroadcaster.repository.table.KneoBroadcasterNameResolver.SOUND_FRAGMENT;

@ApplicationScoped
public class SoundFragmentRepository extends AsyncRepository {
    private static final EntityData entityData = KneoBroadcasterNameResolver.create().getEntityNames(SOUND_FRAGMENT);

    @Inject
    public SoundFragmentRepository(PgPool client, ObjectMapper mapper, RLSRepository rlsRepository) {
        super(client, mapper, rlsRepository);
    }

    public Uni<List<SoundFragment>> getAll(final int limit, final int offset, final IUser user) {
        String sql = "SELECT * FROM " + entityData.getTableName() + " t, " + entityData.getRlsName() + " rls " +
                "WHERE t.id = rls.entity_id AND rls.reader = " + user.getId();
        if (limit > 0) {
            sql += String.format(" LIMIT %s OFFSET %s", limit, offset);
        }
        return client.query(sql)
                .execute()
                .onItem().transformToMulti(rows -> Multi.createFrom().iterable(rows))
                .onItem().transform(this::from)
                .collect().asList();
    }

    public Uni<Integer> getAllCount(IUser user) {
        return getAllCount(user.getId(), entityData.getTableName(), entityData.getRlsName());
    }

    public Uni<SoundFragment> findById(UUID uuid, Long userID) {
        return client.preparedQuery(String.format("SELECT theTable.*, rls.* FROM %s theTable JOIN %s rls ON theTable.id = rls.entity_id " +
                        "WHERE rls.reader = $1 AND theTable.id = $2", entityData.getTableName(), entityData.getRlsName()))
                .execute(Tuple.of(userID, uuid))
                .onItem().transform(RowSet::iterator)
                .onItem().transform(iterator -> {
                    if (iterator.hasNext()) {
                        return from(iterator.next());
                    } else {
                        LOGGER.warn(String.format("No %s found with id: " + uuid, entityData.getTableName()));
                        throw new DocumentHasNotFoundException(uuid);
                    }
                });
    }

    public Uni<SoundFragment> insert(SoundFragment doc, List<FileUpload> files, IUser user) {
        LocalDateTime nowTime = ZonedDateTime.now().toLocalDateTime();
        String sql = String.format("INSERT INTO %s (reg_date, author, last_mod_date, last_mod_user, source, status, file_uri, local_path, type, title, artist, genre, album) VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13) RETURNING id;", entityData.getTableName());
        String filesSql = "INSERT INTO kneobroadcaster__sound_fragments_audio_files (fragment_id, audio_data, type) VALUES ($1, $2, $3)";

        Tuple params = Tuple.of(nowTime, user.getId(), nowTime, user.getId())
                .addString(doc.getSource().name())
                .addInteger(doc.getStatus())
                .addString(doc.getFileUri())
                .addString(doc.getLocalPath())
                .addString(doc.getType().name())
                .addString(doc.getName())
                .addString(doc.getArtist())
                .addString(doc.getGenre())
                .addString(doc.getAlbum());

        String readersSql = String.format("INSERT INTO %s(reader, entity_id, can_edit, can_delete) VALUES($1, $2, $3, $4)", entityData.getRlsName());

        return client.withTransaction(tx -> {
            return tx.preparedQuery(sql)
                    .execute(params)
                    .onItem().transform(result -> result.iterator().next().getUUID("id"))
                    .onFailure().recoverWithUni(t -> Uni.createFrom().failure(t))
                    .onItem().transformToUni(id -> {
                        return tx.preparedQuery(readersSql)
                                .execute(Tuple.of(user.getId(), id, true, true))
                                .onItem().ignore().andContinueWithNull()
                                .onFailure().recoverWithUni(t -> Uni.createFrom().failure(t))
                                .onItem().transformToUni(unused -> {
                                    if (files.isEmpty()) {
                                        return Uni.createFrom().item(id);
                                    }

                                    List<Uni<RowSet<Row>>> fileInserts = new ArrayList<>();
                                    for (FileUpload file : files) {
                                        try {
                                            byte[] fileContent = Files.readAllBytes(Paths.get(file.uploadedFileName()));
                                            Uni<RowSet<Row>> fileInsert = tx.preparedQuery(filesSql)
                                                    .execute(Tuple.of(
                                                            id,
                                                            fileContent,
                                                            file.contentType()
                                                    ));
                                            fileInserts.add(fileInsert);
                                        } catch (IOException e) {
                                            return Uni.createFrom().failure(e);
                                        }
                                    }

                                    return Uni.combine().all().unis(fileInserts)
                                            .discardItems()
                                            .onItem().transform(v -> id);
                                });
                    });
        }).onItem().transformToUni(id -> findById(id, user.getId()));
    }

    public Uni<SoundFragment> update(UUID id, SoundFragment doc, List<FileUpload> files, IUser user) {
        return rlsRepository.findById(entityData.getRlsName(), user.getId(), id)
                .onItem().transformToUni(permissions -> {
                    if (permissions[0]) {
                        String deleteSql = String.format("DELETE FROM %s WHERE fragment_id=$1", entityData.getFilesTableName());
                        String filesSql = String.format("INSERT INTO %s (fragment_id, audio_data, type) VALUES ($1, $2, $3)", entityData.getFilesTableName());

                        return client.withTransaction(tx -> {
                            return tx.preparedQuery(deleteSql)
                                    .execute(Tuple.of(id))
                                    .onItem().transformToUni(ignored -> {
                                        // Insert new files if any
                                        if (!files.isEmpty()) {
                                            List<Uni<RowSet<Row>>> fileInserts = new ArrayList<>();
                                            for (FileUpload file : files) {
                                                try {
                                                    byte[] fileContent = Files.readAllBytes(Paths.get(file.uploadedFileName()));
                                                    Uni<RowSet<Row>> fileInsert = tx.preparedQuery(filesSql)
                                                            .execute(Tuple.of(
                                                                    id,
                                                                    fileContent,
                                                                    file.contentType()
                                                            ));
                                                    fileInserts.add(fileInsert);
                                                } catch (IOException e) {
                                                    return Uni.createFrom().failure(e);
                                                }
                                            }
                                            return Uni.combine().all().unis(fileInserts)
                                                    .discardItems()
                                                    .onItem().transform(v -> id);
                                        }
                                        return Uni.createFrom().item(id);
                                    })
                                    .onItem().transformToUni(unused -> {
                                        // Update the main document
                                        LocalDateTime nowTime = ZonedDateTime.now().toLocalDateTime();
                                        String updateSql = String.format("UPDATE %s SET last_mod_user=$1, last_mod_date=$2, source=$3, status=$4, file_uri=$5, local_path=$6, type=$7, title=$8, artist=$9, genre=$10, album=$11 WHERE id=$12;", entityData.getTableName());

                                        Tuple params = Tuple.of(user.getId(), nowTime)
                                                .addString(doc.getSource().name())
                                                .addInteger(doc.getStatus())
                                                .addString(doc.getFileUri())
                                                .addString(doc.getLocalPath())
                                                .addString(doc.getType().name())
                                                .addString(doc.getName())
                                                .addString(doc.getArtist())
                                                .addString(doc.getGenre())
                                                .addString(doc.getAlbum())
                                                .addUUID(id);

                                        return tx.preparedQuery(updateSql)
                                                .execute(params)
                                                .onItem().transformToUni(rowSet -> {
                                                    if (rowSet.rowCount() == 0) {
                                                        return Uni.createFrom().failure(new DocumentHasNotFoundException(id));
                                                    }
                                                    return findById(id, user.getId());
                                                });
                                    });
                        });
                    } else {
                        return Uni.createFrom().failure(new DocumentModificationAccessException("User does not have edit permission", user.getUserName(), id));
                    }
                });
    }

    public Uni<Integer> delete(UUID uuid, IUser user) {
        return delete(uuid, entityData, user);
    }

    private SoundFragment from(Row row) {
        SoundFragment doc = new SoundFragment();
        setDefaultFields(doc, row);
        doc.setSource(SourceType.valueOf(row.getString("source")));
        doc.setStatus(row.getInteger("status"));
        doc.setFileUri(row.getString("file_uri"));
        doc.setLocalPath(row.getString("local_path"));
        doc.setType(FragmentType.valueOf(row.getString("type")));
        doc.setName(row.getString("title"));
        doc.setArtist(row.getString("artist"));
        doc.setGenre(row.getString("genre"));
        doc.setAlbum(row.getString("album"));
        return doc;
    }
}
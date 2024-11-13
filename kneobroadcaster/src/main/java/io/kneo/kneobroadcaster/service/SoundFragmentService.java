package io.kneo.kneobroadcaster.service;

import io.kneo.core.localization.LanguageCode;
import io.kneo.core.model.user.IUser;
import io.kneo.core.repository.UserRepository;
import io.kneo.core.service.AbstractService;
import io.kneo.core.service.UserService;
import io.kneo.kneobroadcaster.dto.SoundFragmentDTO;
import io.kneo.kneobroadcaster.model.SoundFragment;
import io.kneo.kneobroadcaster.repository.SoundFragmentRepository;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.FileUpload;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Validator;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
@ApplicationScoped
public class SoundFragmentService extends AbstractService<SoundFragment, SoundFragmentDTO> {

    private final SoundFragmentRepository repository;
    Validator validator;

    protected SoundFragmentService() {
        super(null, null);
        this.repository = null;
    }

    @Inject
    public SoundFragmentService(UserRepository userRepository,
                                UserService userService,
                                Validator validator,
                                SoundFragmentRepository repository) {
        super(userRepository, userService);
        this.validator = validator;
        this.repository = repository;
    }

    public Uni<List<SoundFragmentDTO>> getAll(final int limit, final int offset, final IUser user) {
        assert repository != null;
        Uni<List<SoundFragment>> uni = repository.getAll(limit, offset, user);
        return uni.onItem().transform(list -> list.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList()));
    }

    public Uni<Integer> getAllCount(final IUser user) {
        assert repository != null;
        return repository.getAllCount(user);
    }

    @Override
    public Uni<SoundFragmentDTO> getDTO(UUID uuid, IUser user, LanguageCode code) {
        assert repository != null;
        return repository.findById(uuid, user.getId())
                .onItem().transform(this::mapToDTO);
    }

    public Uni<SoundFragment> getById(UUID uuid, IUser user) {
        assert repository != null;
        return repository.findById(uuid, user.getId());
    }

    public Uni<SoundFragmentDTO> upsert(String id, SoundFragmentDTO dto, List<FileUpload> files, IUser user, LanguageCode code) {
        assert repository != null;
        SoundFragment entity = buildEntity(dto);

        if (id == null) {
            return repository.insert(entity, files, user)
                    .onItem().transform(this::mapToDTO);
        } else {
            return repository.update(UUID.fromString(id), entity, files, user)
                    .onItem().transform(this::mapToDTO);
        }
    }

    private SoundFragmentDTO mapToDTO(SoundFragment doc) {
        return SoundFragmentDTO.builder()
                .id(doc.getId())
                .author(userRepository.getUserName(doc.getAuthor()).await().atMost(TIMEOUT))
                .regDate(doc.getRegDate())
                .lastModifier(userRepository.getUserName(doc.getLastModifier()).await().atMost(TIMEOUT))
                .lastModifiedDate(doc.getLastModifiedDate())
                .source(doc.getSource())
                .status(doc.getStatus())
                .fileUri(doc.getFileUri())
                .localPath(doc.getLocalPath())
                .type(doc.getType())
                .name(doc.getName())
                .artist(doc.getArtist())
                .genre(doc.getGenre())
                .album(doc.getAlbum())
                .build();
    }

    private SoundFragment buildEntity(SoundFragmentDTO dto) {
        SoundFragment doc = new SoundFragment();
        doc.setSource(dto.getSource());
        doc.setStatus(dto.getStatus());
        doc.setFileUri(dto.getFileUri());
        doc.setLocalPath(dto.getLocalPath());
        doc.setType(dto.getType());
        doc.setName(dto.getName());
        doc.setArtist(dto.getArtist());
        doc.setGenre(dto.getGenre());
        doc.setAlbum(dto.getAlbum());
        return doc;
    }

    public Uni<Integer> delete(String id, IUser user) {
        assert repository != null;
        return repository.delete(UUID.fromString(id), user);
    }
}
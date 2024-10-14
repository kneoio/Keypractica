package io.kneo.qtracker.service;

import io.kneo.core.localization.LanguageCode;
import io.kneo.core.model.user.IUser;
import io.kneo.core.repository.UserRepository;
import io.kneo.core.service.AbstractService;
import io.kneo.core.service.UserService;
import io.kneo.qtracker.dto.ConsumingDTO;
import io.kneo.qtracker.model.Consuming;
import io.kneo.qtracker.repository.ConsumingRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Validator;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class ConsumingService extends AbstractService<Consuming, ConsumingDTO> {
    private final ConsumingRepository repository;

    Validator validator;

    @Inject
    public ConsumingService(UserRepository userRepository, UserService userService, Validator validator, ConsumingRepository repository) {
        super(userRepository, userService);
        this.validator = validator;
        this.repository = repository;
    }

    public Uni<List<ConsumingDTO>> getAll(int limit, int offset, IUser user) {
        assert repository != null;
        Uni<List<Consuming>> uni = repository.getAll(limit, offset, user);
        return uni
                .onItem().transform(consumingList -> consumingList.stream()
                        .map(consuming -> ConsumingDTO.builder()
                                .id(consuming.getId())
                                .vehicleId(consuming.getVehicleId())
                                .totalKm(consuming.getTotalKm())
                                .lastLiters(consuming.getLastLiters())
                                .lastCost(consuming.getLastCost())
                                .build())
                        .collect(Collectors.toList()));
    }

    public Uni<Integer> getAllCount(IUser user) {
        assert repository != null;
        return repository.getAllCount(user);
    }

    @Override
    public Uni<ConsumingDTO> getDTO(UUID uuid, IUser user, LanguageCode code) {
        assert repository != null;
        Uni<Consuming> consumingUni = repository.findById(uuid);
        return consumingUni.onItem().transformToUni(this::map);
    }

    public Uni<Consuming> getById(UUID uuid, IUser user) {
        assert repository != null;
        return repository.findById(uuid);
    }

    @Override
    public Uni<ConsumingDTO> upsert(String id, ConsumingDTO dto, IUser user, LanguageCode code) {
        assert repository != null;
        if (id == null) {
            return repository.insert(buildEntity(dto), user)
                    .onItem().transformToUni(this::map);
        } else {
            return repository.update(UUID.fromString(id), buildEntity(dto), user)
                    .onItem().transformToUni(this::map);
        }
    }

    private Uni<ConsumingDTO> map(Consuming doc) {
        return Uni.createFrom().item(() -> ConsumingDTO.builder()
                .id(doc.getId())
                .vehicleId(doc.getVehicleId())
                .totalKm(doc.getTotalKm())
                .lastLiters(doc.getLastLiters())
                .lastCost(doc.getLastCost())
                .build());
    }

    private Consuming buildEntity(ConsumingDTO dto) {
        Consuming doc = new Consuming();
        doc.setVehicleId(dto.getVehicleId());
        doc.setTotalKm(dto.getTotalKm());
        doc.setLastLiters(dto.getLastLiters());
        doc.setLastCost(dto.getLastCost());
        return doc;
    }

    public Uni<Integer> delete(String id, IUser user) {
        assert repository != null;
        return repository.delete(UUID.fromString(id), user);
    }
}

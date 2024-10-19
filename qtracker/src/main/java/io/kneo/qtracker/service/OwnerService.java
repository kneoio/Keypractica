package io.kneo.qtracker.service;

import io.kneo.core.localization.LanguageCode;
import io.kneo.core.model.user.IUser;
import io.kneo.core.repository.UserRepository;
import io.kneo.core.repository.exception.DocumentModificationAccessException;
import io.kneo.core.service.AbstractService;
import io.kneo.core.service.UserService;
import io.kneo.qtracker.dto.OwnerDTO;
import io.kneo.qtracker.dto.VehicleDTO;
import io.kneo.qtracker.model.Owner;
import io.kneo.qtracker.repository.OwnerRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Validator;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class OwnerService extends AbstractService<Owner, OwnerDTO> {
    private final OwnerRepository repository;
    private final VehicleService vehicleService;

    Validator validator;

    protected OwnerService() {
        super(null, null);
        this.repository = null;
        this.vehicleService = null;
    }

    @Inject
    public OwnerService(UserRepository userRepository,
                        UserService userService,
                        Validator validator,
                        OwnerRepository repository,
                        VehicleService vehicleService) {
        super(userRepository, userService);
        this.validator = validator;
        this.repository = repository;
        this.vehicleService = vehicleService;
    }

    public Uni<List<OwnerDTO>> getAll(final int limit, final int offset, final IUser user) {
        assert repository != null;
        Uni<List<Owner>> uni = repository.getAll(limit, offset, user);
        return uni
                .onItem().transform(ownerList -> ownerList.stream()
                        .map(owner -> OwnerDTO.builder()
                                .id(owner.getId())
                                .author(userRepository.getUserName(owner.getAuthor()))
                                .regDate(owner.getRegDate())
                                .lastModifier(userRepository.getUserName(owner.getLastModifier()))
                                .lastModifiedDate(owner.getLastModifiedDate())
                                .email(owner.getEmail())
                                .telegramName(owner.getTelegramName())
                                .whatsappName(owner.getWhatsappName())
                                .phone(owner.getPhone())
                                .build())
                        .collect(Collectors.toList()));
    }

    public Uni<Integer> getAllCount(final IUser user) {
        assert repository != null;
        return repository.getAllCount(user);
    }

    @Override
    public Uni<OwnerDTO> getDTO(UUID uuid, IUser user, LanguageCode code) {
        assert repository != null;
        Uni<Owner> ownerUni = repository.findById(uuid, user.getId());
        return ownerUni.onItem().transformToUni(this::map);
    }

    public Uni<Owner> getById(UUID uuid, IUser user) {
        assert repository != null;
        return repository.findById(uuid, user.getId());
    }

    @Override
    public Uni<OwnerDTO> upsert(String id, OwnerDTO dto, IUser user, LanguageCode code) throws DocumentModificationAccessException {
        assert repository != null;
        assert vehicleService != null;

        if (id == null) {
            Uni<OwnerDTO> ownerDTOUni = repository.insert(buildEntity(dto), user)
                    .onItem().transformToUni(this::map);

            return ownerDTOUni
                    .onItem().transformToUni(ownerDTO -> {
                        return vehicleService.upsert(null, VehicleService.getTemporaryVehicle(ownerDTO), user, code)
                                .replaceWith(ownerDTO);
                    });
        } else {
            return repository.update(UUID.fromString(id), buildEntity(dto), user)
                    .onItem().transformToUni(this::map);
        }
    }


    private Uni<OwnerDTO> map(Owner doc) {
        return Uni.createFrom().item(() -> OwnerDTO.builder()
                .id(doc.getId())
                .author(userRepository.getUserName(doc.getAuthor()))
                .regDate(doc.getRegDate())
                .lastModifier(userRepository.getUserName(doc.getLastModifier()))
                .lastModifiedDate(doc.getLastModifiedDate())
                .localizedName(doc.getLocalizedName())
                .email(doc.getEmail())
                .telegramName(doc.getTelegramName())
                .whatsappName(doc.getWhatsappName())
                .phone(doc.getPhone())
                .localizedName(doc.getLocalizedName())
                .country(doc.getCountry())
                .currency(doc.getCurrency())
                .birthDate(doc.getBirthDate())
                .build());
    }

    private Owner buildEntity(OwnerDTO dto) {
        Owner doc = new Owner();
        doc.setEmail(dto.getEmail());
        doc.setTelegramName(dto.getTelegramName());
        doc.setWhatsappName(dto.getWhatsappName());
        doc.setLocalizedName(dto.getLocalizedName());
        doc.setPhone(dto.getPhone());
        doc.setCountry(dto.getCountry());
        doc.setCurrency(dto.getCurrency());
        doc.setBirthDate(dto.getBirthDate());
        return doc;
    }

    public Uni<Integer> delete(String id, IUser user) {
        assert repository != null;
        return repository.delete(UUID.fromString(id), user);
    }

    public Uni<Owner> getByTelegramId(String id, IUser user, LanguageCode languageCode) {
        assert repository != null;
        return  repository.findByTelegramId(id, user.getId());
    }

    public Uni<OwnerDTO> getDTOByTelegramId(String id, IUser user, LanguageCode languageCode) {
        assert repository != null;
        assert vehicleService != null;
        Uni<Owner> ownerUni = repository.findByTelegramId(id, user.getId());
        return ownerUni.onItem().transformToUni(owner -> {
            Uni<OwnerDTO> ownerDTOUni = map(owner);
            Uni<List<VehicleDTO>> vehiclesUni = vehicleService.getOwnedBy(owner.getId(), user)
                    .onItem().transformToUni(vehicles -> {
                        List<Uni<VehicleDTO>> vehicleDTOUnis = vehicles.stream()
                                .map(vehicleService::map)
                                .toList();
                        return Uni.combine().all().unis(vehicleDTOUnis).with(results -> (List<VehicleDTO>) results);
                    });

            return ownerDTOUni.onItem().transformToUni(ownerDTO ->
                    vehiclesUni.onItem().transform(vehicles -> {
                        ownerDTO.setVehicles(vehicles);
                        return ownerDTO;
                    })
            );
        });
    }

}

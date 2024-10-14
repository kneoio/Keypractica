package io.kneo.qtracker.service;

import io.kneo.core.localization.LanguageCode;
import io.kneo.core.model.user.IUser;
import io.kneo.core.repository.UserRepository;
import io.kneo.core.service.AbstractService;
import io.kneo.core.service.UserService;
import io.kneo.qtracker.dto.VehicleDTO;
import io.kneo.qtracker.model.Vehicle;
import io.kneo.qtracker.repository.VehicleRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Validator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class VehicleService extends AbstractService<Vehicle, VehicleDTO> {
    private final VehicleRepository repository;

    Validator validator;

    protected VehicleService() {
        super(null, null);
        this.repository = null;
    }

    @Inject
    public VehicleService(UserRepository userRepository, UserService userService, Validator validator, VehicleRepository repository) {
        super(userRepository, userService);
        this.validator = validator;
        this.repository = repository;
    }

    public Uni<List<VehicleDTO>> getAll(final int limit, final int offset, final IUser user) {
        assert repository != null;
        Uni<List<Vehicle>> uni = repository.getAll(limit, offset, user);
        return uni
                .onItem().transform(vehicleList -> vehicleList.stream()
                        .map(vehicle -> VehicleDTO.builder()
                                .id(vehicle.getId())
                                .author(userRepository.getUserName(vehicle.getAuthor()))
                                .regDate(vehicle.getRegDate())
                                .lastModifier(userRepository.getUserName(vehicle.getLastModifier()))
                                .lastModifiedDate(vehicle.getLastModifiedDate())
                                .vin(vehicle.getVin())
                                .vehicleType(vehicle.getVehicleType())
                                .fuelType(vehicle.getFuelType())
                                .brand(vehicle.getBrand())
                                .model(vehicle.getModel())
                                .localizedName(vehicle.getLocalizedName())
                                .build())
                        .collect(Collectors.toList()));
    }

    public Uni<Integer> getAllCount(final IUser user) {
        assert repository != null;
        return repository.getAllCount(user);
    }

    @Override
    public Uni<VehicleDTO> getDTO(UUID uuid, IUser user, LanguageCode code) {
        assert repository != null;
        Uni<Vehicle> vehicleUni = repository.findById(uuid, user.getId());
        return vehicleUni.onItem().transformToUni(this::map);
    }

    public Uni<Vehicle> getById(UUID uuid, IUser user) {
        assert repository != null;
        return repository.findById(uuid, user.getId());
    }

    @Override
    public Uni<VehicleDTO> upsert(String id, VehicleDTO dto, IUser user, LanguageCode code) {
        assert repository != null;
        if (id == null) {
            return repository.insert(buildEntity(dto), user)
                    .onItem().transformToUni(this::map);
        } else {
            return repository.update(UUID.fromString(id), buildEntity(dto), user)
                    .onItem().transformToUni(this::map);
        }
    }

    private Uni<VehicleDTO> map(Vehicle doc) {
        return Uni.createFrom().item(() -> VehicleDTO.builder()
                .id(doc.getId())
                .author(userRepository.getUserName(doc.getAuthor()))
                .regDate(doc.getRegDate())
                .lastModifier(userRepository.getUserName(doc.getLastModifier()))
                .lastModifiedDate(doc.getLastModifiedDate())
                .localizedName(doc.getLocalizedName())
                .vin(doc.getVin())
                .vehicleType(doc.getVehicleType())
                .fuelType(doc.getFuelType())
                .brand(doc.getBrand())
                .model(doc.getModel())
                .ownerId(doc.getOwnerId())
                .status(doc.getStatus())
                .build());
    }

    private Vehicle buildEntity(VehicleDTO dto) {
        Vehicle doc = new Vehicle();
        doc.setOwnerId(dto.getOwnerId());
        doc.setVin(dto.getVin());
        doc.setVehicleType(dto.getVehicleType());
        doc.setBrand(dto.getBrand());
        doc.setModel(dto.getModel());
        doc.setFuelType(dto.getFuelType());
        doc.setLocalizedName(dto.getLocalizedName());
        return doc;
    }

    public Uni<Integer> delete(String id, IUser user) {
        assert repository != null;
        return repository.delete(UUID.fromString(id), user);
    }
}

package io.kneo.projects.grpc;

import io.kneo.core.model.user.AiAgentUser;
import io.kneo.core.model.user.IUser;
import io.kneo.projects.dto.ProjectDTO;
import io.kneo.projects.service.ProjectService;
import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;

@GrpcService
public class ProjectGrpcServiceImpl implements io.kneo.projects.grpc.ProjectGrpcService {

    @Inject
    ProjectService projectService;

    @Override
    public Uni<io.kneo.projects.grpc.ProjectResponse> addProject(io.kneo.projects.grpc.ProjectRequest request) {
        ProjectDTO dto = ProjectDTO.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();

        return projectService.add(dto, getCurrentUser())
                .onItem().transform(uuid ->
                        io.kneo.projects.grpc.ProjectResponse.newBuilder()
                                .setId(uuid.toString())
                                .build()
                );
    }

    private IUser getCurrentUser() {
        return AiAgentUser.build();
    }
}
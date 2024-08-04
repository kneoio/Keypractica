package io.kneo.projects.grpc;

import io.kneo.core.model.user.AiAgentUser;
import io.kneo.core.model.user.IUser;
import io.kneo.grpc.stubs.project.ProjectGrpcService;
import io.kneo.grpc.stubs.project.ProjectRequest;
import io.kneo.grpc.stubs.project.ProjectResponse;
import io.kneo.projects.dto.ProjectDTO;
import io.kneo.projects.service.ProjectService;
import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;

@GrpcService
public class ProjectGrpcServiceImpl implements ProjectGrpcService {

    @Inject
    ProjectService projectService;

    @Override
    public Uni<ProjectResponse> addProject(ProjectRequest request) {
        ProjectDTO dto = ProjectDTO.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();

        return projectService.upsert(String.valueOf(dto.getId()), dto, getCurrentUser())
                .onItem().transform(uuid ->
                        ProjectResponse.newBuilder()
                                .setId(uuid.toString())
                                .build()
                );
    }

    private IUser getCurrentUser() {
        return AiAgentUser.build();
    }
}
package io.kneo.projects.grpc;

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
        return null;
    }
}
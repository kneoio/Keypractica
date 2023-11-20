package io.kneo.core.grpc;

import com.google.protobuf.Empty;
import io.kneo.core.model.user.User;
import io.kneo.core.service.UserService;
import io.kneo.keypractica.grpc.dictionary.user.UserGRPCService;
import io.kneo.keypractica.grpc.dictionary.user.UserProto;
import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Multi;
import jakarta.inject.Inject;

@GrpcService
public class UserGRPCServiceImpl implements UserGRPCService {

    @Inject
    UserService service;


    @Override
    public Multi<UserProto> getAllUsers(Empty request) {
        Multi<User> multiUsers = (Multi<User>) service.getAll();
        Multi<UserProto> multiUserProtos = multiUsers
                .map(user -> UserProto.newBuilder()
                        .setId("33L")
                        .setLogin(user.getUserName())
//                        .setEmail(user.getEmail())
                        .build());
        return multiUserProtos;
    }
}

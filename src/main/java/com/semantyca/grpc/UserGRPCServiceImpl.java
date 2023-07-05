package com.semantyca.grpc;

import com.google.protobuf.Empty;
import com.semantyca.jirascope.grpc.dictionary.user.UserGRPCService;
import com.semantyca.jirascope.grpc.dictionary.user.UserProto;
import com.semantyca.model.user.User;
import com.semantyca.service.UserService;
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
                        .setLogin(user.getLogin())
//                        .setEmail(user.getEmail())
                        .build());
        return multiUserProtos;
    }
}

package io.kneo.core.grpc;

import io.kneo.core.service.LanguageService;
import io.kneo.keypractica.grpc.dictionary.lang.Empty;
import io.kneo.keypractica.grpc.dictionary.lang.LanguageGrpcService;
import io.kneo.keypractica.grpc.dictionary.lang.LanguageProtoList;
import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
@GrpcService
public class LanguageGrpcServiceImpl implements LanguageGrpcService {

    @Inject
    LanguageService service;

    @Override
    public Uni<LanguageProtoList> getAll(Empty request) {
        /*Multi<Language> languages = service.getAll();
        Multi<LanguageProto> multiUserProtos = languages
                .map(user -> UserProto.newBuilder()
                        .setId("33L")
                        .setLogin(user.getLogin())
//                        .setEmail(user.getEmail())
                        .build());
        return multiUserProtos;*/
       return null;
    }
}

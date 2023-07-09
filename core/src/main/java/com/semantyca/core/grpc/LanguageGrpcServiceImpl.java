package com.semantyca.core.grpc;

import com.semantyca.core.service.LanguageService;
import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
@GrpcService
public class LanguageGrpcServiceImpl implements com.semantyca.jirascope.grpc.dictionary.lang.LanguageGrpcService {

    @Inject
    LanguageService service;

    @Override
    public Uni<com.semantyca.jirascope.grpc.dictionary.lang.LanguageProtoList> getAll(com.semantyca.jirascope.grpc.dictionary.lang.Empty request) {
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

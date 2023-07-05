package com.semantyca.grpc;


import com.semantyca.jirascope.grpc.dictionary.lang.Empty;
import com.semantyca.jirascope.grpc.dictionary.lang.LanguageGrpcService;
import com.semantyca.jirascope.grpc.dictionary.lang.LanguageProtoList;
import com.semantyca.service.LanguageService;
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

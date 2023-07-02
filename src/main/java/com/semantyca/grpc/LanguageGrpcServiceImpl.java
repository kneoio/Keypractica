package com.semantyca.grpc;

import com.semantyca.jirascope.grpc.dictionary.Empty;
import com.semantyca.jirascope.grpc.dictionary.LanguageGrpcService;
import com.semantyca.jirascope.grpc.dictionary.LanguageProtoList;
import com.semantyca.jirascope.grpc.dictionary.MapWrapper;
import com.semantyca.model.Language;
import com.semantyca.service.LanguageService;
import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;

import java.util.List;
@GrpcService
public class LanguageGrpcServiceImpl implements LanguageGrpcService {

    @Inject
    LanguageService service;

    @Override
    public Uni<LanguageProtoList> getAll(Empty request) {
        List<Language> languages = service.getAll();
        List<MapWrapper> iterable = languages.stream()
                .map(v -> {
                    MapWrapper mapWrapper = MapWrapper.newBuilder()
                            .putAllNamesAndValues(v.getValuesAsMap())
                            .build();
                    return mapWrapper;
                }).toList();
        return Uni.createFrom().item(() ->
                LanguageProtoList.newBuilder().addAllFieldsMap(iterable).build()
        );
    }
}

package io.kneo.projects.service;

import io.kneo.core.model.user.IUser;
import io.kneo.grpc.stubs.ai.AiServiceGrpc;
import io.kneo.grpc.stubs.ai.AiServiceOuterClass;
import io.kneo.projects.dto.ai.PromptDTO;
import io.quarkus.grpc.GrpcClient;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AiService  {

    @GrpcClient("claude")
    AiServiceGrpc.AiServiceBlockingStub client;


    public Uni<String> chat(PromptDTO prompt, IUser user) {
        System.out.println(prompt);
        return Uni.createFrom().item(() -> {
            AiServiceOuterClass.AiRequest request = AiServiceOuterClass.AiRequest.newBuilder()
                    .setPrompt(prompt.getPromptText())
                    .setApiKey("1234567890")
                    .setSessionId("12654")
                    .build();

            AiServiceOuterClass.AiResponse response = client.generateAiResponse(request);
            System.out.println(response);
            return response.getResponse();
        }).runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    }



}

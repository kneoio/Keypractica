package com.semantyca.core.grpc;

import com.google.protobuf.Empty;
import com.semantyca.core.grpc.service.messaging.EmailService;
import com.semantyca.core.grpc.service.messaging.MessagingProto;
import com.semantyca.core.service.messaging.email.MailAgent;
import com.semantyca.core.service.messaging.exception.MsgException;
import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;

import java.util.Collections;

@GrpcService
public class MessagingGRPCServiceImpl implements EmailService {

    @Inject
    MailAgent service;


    @Override
    public Uni<MessagingProto> sendTestMessage(Empty request) {
        try {
            service.sendMessage(Collections.singletonList("justaidajam@gmail.com"), "Test message", "Test")
                    .join();
        } catch (MsgException e) {
            throw new RuntimeException(e);
        }
        return Uni.createFrom().item(MessagingProto.getDefaultInstance());
    }
}

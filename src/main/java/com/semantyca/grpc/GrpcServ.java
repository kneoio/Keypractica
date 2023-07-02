package com.semantyca.grpc;

import com.semantyca.jirascope.grpc.HelloReply;
import com.semantyca.jirascope.grpc.HelloRequest;
import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Uni;

@GrpcService
class GrpcServ implements com.semantyca.jirascope.grpc.Greeter {
    @Override
    public Uni<HelloReply> sayHello(HelloRequest request) {
        return Uni.createFrom().item(() ->
                HelloReply.newBuilder().setMessage("Hello " + request.getName()).build()
        );
    }
}
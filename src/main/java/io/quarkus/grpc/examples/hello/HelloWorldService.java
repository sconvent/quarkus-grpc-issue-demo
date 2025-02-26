package io.quarkus.grpc.examples.hello;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import examples.Greeter;
import examples.HelloReply;
import examples.HelloRequest;
import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.infrastructure.Infrastructure;

@GrpcService
public class HelloWorldService implements Greeter {

    public static final int COUNT = 100000;

    @Override
    public Multi<HelloReply> sayHello(HelloRequest request) {
        Stream<HelloReply> inputs = IntStream.rangeClosed(0, COUNT - 1)
                .mapToObj(it -> HelloReply.newBuilder().setMessage(buildTestString(it)).build());
        Multi<HelloReply> multi = Multi.createFrom().emitter(multiEmitter -> {
            inputs.forEach(multiEmitter::emit);
            multiEmitter.complete();
        });
        return multi
                .emitOn(Infrastructure.getDefaultWorkerPool())
                .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
                .onRequest().invoke(requestSize -> System.out.println("Server request size: " + requestSize));
    }

    private String buildTestString(int n) {
        String smallString = String.format("%10d", n);
        return smallString.repeat(1000);
    }
}

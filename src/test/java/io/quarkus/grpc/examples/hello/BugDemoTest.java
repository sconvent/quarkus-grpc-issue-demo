package io.quarkus.grpc.examples.hello;

import examples.Greeter;
import examples.HelloRequest;
import io.quarkus.grpc.GrpcClient;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static io.quarkus.grpc.examples.hello.HelloWorldService.COUNT;
import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class BugDemoTest {

    @GrpcClient
    Greeter greeter;

    @Test
    public void bugDemoTest() {
        AtomicBoolean firstRequestLogged = new AtomicBoolean(false);

        Long count = greeter.sayHello(HelloRequest.newBuilder().setName("test").build())
                .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
                .onRequest().invoke(requestSize -> {
                    // Log only the first request (or there will be too many)
                    if(firstRequestLogged.compareAndSet(false, true))
                        System.out.println("Client request size: " + requestSize);
                })
                // Simulate processing time for every item
                .onItem().transformToUniAndMerge(item -> Uni.createFrom().item(item).onItem().delayIt().by(Duration.ofMillis(200)))
                .collect().with(Collectors.counting())
                .await().atMost(Duration.ofSeconds(30));

        assertThat(count).isEqualTo(COUNT);
    }
}

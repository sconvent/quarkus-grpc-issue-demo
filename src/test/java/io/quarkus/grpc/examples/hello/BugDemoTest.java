package io.quarkus.grpc.examples.hello;

import examples.Greeter;
import examples.HelloRequest;
import io.quarkus.grpc.GrpcClient;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static io.quarkus.grpc.examples.hello.HelloWorldService.COUNT;

@QuarkusTest
class BugDemoTest {

    @GrpcClient
    Greeter greeter;

    @Test
    public void bugDemoTest() {
        AtomicBoolean firstRequestLogged = new AtomicBoolean(false);
        AtomicInteger counter = new AtomicInteger(0);

        greeter.sayHello(HelloRequest.newBuilder().setName("test").build())
                .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
                .onRequest().invoke(requestSize -> {
                    // Log only the first request (or there will be too many)
                    if(firstRequestLogged.compareAndSet(false, true))
                        System.out.println("Client request size: " + requestSize);
                })
                .subscribe().with(
                        item -> {
                            counter.incrementAndGet();
                            // Simulate processing time for every item
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        },
                        Throwable::printStackTrace
                );

        Awaitility.await().atMost(Duration.ofSeconds(60)).until(() -> counter.get() == COUNT);
    }
}

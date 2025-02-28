package io.quarkus.grpc.examples.hello;

import examples.Greeter;
import examples.HelloReply;
import examples.HelloRequest;
import io.quarkus.grpc.GrpcClient;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.Flow;
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
                .subscribe().withSubscriber(new Flow.Subscriber<HelloReply>() {

                    private Flow.Subscription subscription;

                    @Override
                    public void onSubscribe(Flow.Subscription subscription) {
                        System.out.println("onSubscribe");
                        this.subscription = subscription;
                        this.subscription.request(1);
                    }

                    @Override
                    public void onNext(HelloReply item) {
                        //System.out.println("onNext");
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        this.subscription.request(1);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        System.out.println("onError");
                    }

                    @Override
                    public void onComplete() {
                        System.out.println("onComplete");
                    }
                });

        Awaitility.await().atMost(Duration.ofSeconds(60)).until(() -> counter.get() == COUNT);
    }
}

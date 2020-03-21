package com.example.demo;

import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import lombok.Builder;
import org.assertj.core.api.Assertions;
import org.junit.runner.RunWith;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.core.io.buffer.NettyDataBufferFactory;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@RunWith(SpringJUnit4ClassRunner.class)
public class Test {
    String body = "{\"id\"=\"1\"}";

    @org.junit.Test
    public void test1() throws InterruptedException{
        Flux<Long> flux = Flux.interval(Duration.ofMillis(100)).take(9).
        doOnNext((element)-> System.out.println("Next: " + element + " " + System.currentTimeMillis())).
        doOnComplete(()-> {System.out.println("End of Flux!!");})
                .subscribeOn(Schedulers.immediate());//.subscribe();

        int count = 0;
        StepVerifier.create(flux)
                .expectNext(0L,1L,2L,3L,4L,5L,6L,7L,8L)
                .expectComplete()
            .verify();
    }

    @org.junit.Test
    public void test2() {
        ByteBufAllocator byteBufAllocator = new PooledByteBufAllocator();
        DataBufferFactory bufferFactory = new DefaultDataBufferFactory();
        DataBuffer dataBuffer = bufferFactory.wrap(body.getBytes());
        Flux<DataBuffer> f = Flux.just(dataBuffer);
    }

    @org.junit.Test
    public void test3() throws InterruptedException{
        String[] a = new String[]{"foo","bar"};
        Flux f = Flux.error(new IllegalStateException()).subscribeOn(Schedulers.immediate());
        StepVerifier.create(f)
                .expectError(IllegalStateException.class).verify();
        Thread.sleep(1200);
    }

    @org.junit.Test
    public void test4() throws InterruptedException{
        Optional<String> opt = Optional.empty();
        Mono<String> mono = Mono.justOrEmpty(opt).doOnSuccess((e)-> {System.out.println("End of Mono!!: " + e);});
        StepVerifier.create(mono).expectComplete().verify();
        Thread.sleep(1200);
    }

    @org.junit.Test
    public void test5() throws InterruptedException{
        Mono.never().doOnSuccess((e)-> {System.out.println("End of Mono!!: " + e);}).subscribe();
        Thread.sleep(1200);

        StepVerifier.withVirtualTime(() -> Mono.delay(Duration.ofHours(3)))
                .expectSubscription()
                .expectNoEvent(Duration.ofHours(2))
                .thenAwait(Duration.ofHours(1))
                .expectNextCount(1)
                .expectComplete()
                .verify();
    }

    @org.junit.Test
    public void test6() {
        Mono<User> mono = Mono.just(User.builder().name("marco").build())
                .doOnNext((u)-> System.out.println("onNext1: " + u.name))
                .map((u)-> User.builder().name(u.name.toUpperCase()).build())
                .doOnNext((u)-> System.out.println("onNext2: " + u.name));
        StepVerifier.create(mono).expectNextMatches((a)-> a.name.equals("MARCO")).expectComplete().verify();
    }

    @Builder
    private static class User {
        private String name;
    }
}
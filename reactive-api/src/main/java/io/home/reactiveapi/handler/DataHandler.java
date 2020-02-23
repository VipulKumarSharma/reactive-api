package io.home.reactiveapi.handler;

import io.home.reactiveapi.model.Restaurant;
import lombok.extern.log4j.Log4j2;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Signal;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.util.context.Context;

import java.util.Collection;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.stream.IntStream;

@Log4j2
@Component
public class DataHandler {

    private final String UID = "uid";
    private Scheduler SCHEDULER = Schedulers.fromExecutor(Executors.newFixedThreadPool(10));

    private final Collection<Restaurant> restaurants = new ConcurrentSkipListSet<>((o1, o2) -> {
        Double one = o1.getPricePerPerson();
        Double two = o2.getPricePerPerson();
        return one.compareTo(two);
    });

    DataHandler() {
        IntStream.range(0, 1000)
                .mapToObj(Integer::toString)
                .map(i-> "restaurant#"+i)
                .map(str -> new Restaurant(str, new Random().nextDouble()))
                .forEach(this.restaurants::add);
    }

    public Mono<ServerResponse> getData(ServerRequest request) {
        Flux<String> letters = prepare(Flux.just("A","B"));
        Flux<Integer> numbers = prepare(Flux.just(1,2,3,4));

        Flux<String> combined =
                prepare(
                        Flux.zip(letters, numbers)
                                .map(tuple -> tuple.getT1()+"::"+tuple.getT2())
                )
                        .doOnEach(signal -> {
                            if(!signal.isOnNext()) {
                                return;
                            }
                            Context ctx = signal.getContext();
                            Object userId = ctx.get("userId");
                            log.info("userId for this pipeline stage for data '"+signal.get()+"' is '"+userId+"'");
                        })
                        .subscriberContext(Context.of("userId", UUID.randomUUID().toString()));

        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(combined, String.class);
    }

    private <T> Flux<T> prepare(Flux<T> in) {
        return in.doOnNext(log::info)
                .subscribeOn(SCHEDULER);
    }

    public Mono<ServerResponse> getRestaurantsByMaxPrice(ServerRequest request) {
        String uid = request.pathVariable(UID);
        double price = Double.parseDouble(request.pathVariable("price"));

        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(
                        adaptResults(getByMaxPrice(price), uid, price),
                        Restaurant.class
                );
    }

    private Flux<Restaurant> getByMaxPrice(double maxPrice) {
        return Flux.fromStream(
                this.restaurants
                        .parallelStream()
                        .filter(r -> r.getPricePerPerson() <= maxPrice)
        );
    }

    private Flux<Restaurant> adaptResults(Flux<Restaurant> in, String uid, double price) {

        return Mono.just(String.format("finding restaurants having price lower than $%.2f for %s", price, uid))
                .doOnEach(logOnNext(log::info))
                .thenMany(in)
                .doOnEach(
                        logOnNext(r -> log.info("found restaurant {} for ${}", r.getName(), r.getPricePerPerson()))
                )
                .subscriberContext(Context.of(UID, uid));
    }

    private <T> Consumer<Signal<T>> logOnNext (Consumer<T> logStatement) {
        return signal -> {
            if(!signal.isOnNext()) {
                return;
            }

            Optional<String> uidOptional = signal.getContext().getOrEmpty(UID);

            Runnable orElse = () -> logStatement.accept(signal.get());

            Consumer<String> ifPresent = uid -> {
                try  (MDC.MDCCloseable closeable = MDC.putCloseable(UID, uid)) {
                    orElse.run();
                }
            };

            uidOptional.ifPresentOrElse(ifPresent, orElse);
        };
    }

}

package io.home.reactiveapi.handler;

import io.home.reactiveapi.model.EmployeeEvent;
import io.home.reactiveapi.persistence.Employee;
import io.home.reactiveapi.repository.EmployeeRepository;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.stream.Stream;

@Service
public class EmployeeHandler {

    private final EmployeeRepository employeeRepository;

    public EmployeeHandler(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public Mono<ServerResponse> getAllEmployees(ServerRequest request) {
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(employeeRepository.findAll(), Employee.class);
    }

    public Mono<ServerResponse> getEmployeeById(ServerRequest request) {
        long id = Long.valueOf(request.pathVariable("id"));

        return employeeRepository.findById(id)
                .flatMap(e -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(e, Employee.class)
                )
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> createNewEmployee(ServerRequest request) {
        Mono<Employee> employeeMono = request.bodyToMono(Employee.class);
        Mono<Employee> employee = employeeRepository.save(employeeMono.block());

        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(employee, Employee.class);
    }

    public Mono<ServerResponse> deleteEmployee(ServerRequest request) {
        long id = Long.parseLong(request.pathVariable("id"));

        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .build(employeeRepository.deleteById(id));
    }

    public Mono<ServerResponse> getEmployeeEvents(ServerRequest request) {
        long id = Long.parseLong(request.pathVariable("id"));

        Flux<EmployeeEvent> empEvents =  employeeRepository.findById(id)
                .flatMapMany(employee -> {
                    Flux<Long> interval = Flux.interval(Duration.ofMillis(500));

                    Flux<EmployeeEvent> employeeEventFlux = Flux.fromStream(
                            Stream.generate(() -> new EmployeeEvent(employee, LocalDateTime.now()))
                    );

                    return Flux.zip(interval, employeeEventFlux)
                            .map(Tuple2::getT2);
                });

        return ServerResponse.ok()
                .contentType(MediaType.TEXT_EVENT_STREAM)
                .body(empEvents, EmployeeEvent.class);

    }
}

package io.home.reactiveapi.handler;

import io.home.reactiveapi.persistence.Employee;
import io.home.reactiveapi.repository.EmployeeRepository;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class EmployeeHandler {

    private final EmployeeRepository employeeRepository;

    public EmployeeHandler(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public Mono<ServerResponse> getAllEmployees(ServerRequest request) {
        Flux<Employee> employees = employeeRepository.findAll();
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(employees, Employee.class);
    }

    public Mono<ServerResponse> getEmployeeById(ServerRequest request) {
        long id = Long.parseLong(request.pathVariable("id"));
        Mono<Employee> employee = employeeRepository.findById(id);

        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(employee, Employee.class);
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
        Mono<Void> employee = employeeRepository.deleteById(id);

        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .build(employee);
    }
}

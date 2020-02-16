package io.home.reactiveapi;

import io.home.reactiveapi.persistence.Employee;
import io.home.reactiveapi.repository.EmployeeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.stream.Stream;

@SpringBootApplication
public class ReactiveApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReactiveApiApplication.class, args);
	}

	/*@Bean
	CommandLineRunner employees (EmployeeRepository employeeRepository) {
		return args -> {
			employeeRepository
					.deleteAll()
					.subscribe(null, null, () -> {
						Stream.of(
								new Employee(5L, "Employee #5", "50000"),
								new Employee(6L, "Employee #6", "60000")
						).forEach(employee -> {
							employeeRepository
									.save(employee)
									.subscribe(System.out::println);
						});
					});
		};
	}*/
}

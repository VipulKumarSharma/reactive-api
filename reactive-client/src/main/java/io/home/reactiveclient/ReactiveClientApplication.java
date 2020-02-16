package io.home.reactiveclient;

import io.home.reactiveclient.model.Employee;
import io.home.reactiveclient.model.EmployeeEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
public class ReactiveClientApplication {

    @Value("${config.server.url}")
    String serverURL;

    @Bean
    WebClient webClient() {
        return WebClient.create(serverURL);
    }

    @Bean
    CommandLineRunner commandLineRunner(WebClient webClient) {
        return strings -> {
            webClient
                .get()
                .uri("/emp")
                .retrieve()
                .bodyToFlux(Employee.class)
                .filter(employee -> employee.getId() == 2L)
                .flatMap(employee -> webClient
                                        .get()
                                        .uri("emp/{id}/events", employee.getId())
                                        .retrieve()
                                        .bodyToFlux(EmployeeEvent.class)
                )
                .subscribe(System.out::println);

            for (int i = 0; i < 100; i++) {
                System.out.println("Blocking Code : " + i);
                Thread.sleep(1000);
            }
        };
    }

    public static void main(String[] args) {
        SpringApplication.run(ReactiveClientApplication.class, args);
    }

}

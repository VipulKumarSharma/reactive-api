package io.home.reactiveapi.handler;

import io.home.reactiveapi.persistence.Employee;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class AppHandler {

    @Value("${config.app.name}")
    private String appName;

    public Mono<ServerResponse> getAppName(ServerRequest request) {
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.justOrEmpty(appName), String.class);
    }
}

package io.home.reactiveapi.router;

import io.home.reactiveapi.handler.AppHandler;
import io.home.reactiveapi.handler.EmployeeHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration
public class AppRouter {

    @Value("${config.app.name}")
    private String appName;

    @Bean
    public RouterFunction<ServerResponse> route(EmployeeHandler handler, AppHandler appHandler) {
        return RouterFunctions
                .route(
                        RequestPredicates.GET("/"),
                        req -> ServerResponse.ok().body(Mono.justOrEmpty(appName+" is in running state"), String.class))
                .andRoute(
                        RequestPredicates.GET("/appName"),
                        appHandler::getAppName)

                /* Employee Endpoints */
                .andRoute(
                        RequestPredicates.GET("/emp").and(RequestPredicates.accept(MediaType.APPLICATION_JSON)),
                        handler::getAllEmployees)
                .andRoute(
                        RequestPredicates.GET("/emp/{id}").and(RequestPredicates.accept(MediaType.APPLICATION_JSON)),
                        handler::getEmployeeById)
                .andRoute(
                        RequestPredicates.POST("/emp").and(RequestPredicates.accept(MediaType.APPLICATION_JSON)),
                        handler::createNewEmployee)
                .andRoute(
                        RequestPredicates.DELETE("/emp/{id}").and(RequestPredicates.accept(MediaType.APPLICATION_JSON)),
                        handler::deleteEmployee)
                .andRoute(
                        RequestPredicates.GET("/emp/{id}/events").and(RequestPredicates.accept(MediaType.APPLICATION_JSON)),
                        handler::getEmployeeEvents);
    }
}

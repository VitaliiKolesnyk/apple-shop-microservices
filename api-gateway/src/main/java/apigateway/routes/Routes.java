package apigateway.routes;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import java.net.URI;

import static org.springframework.cloud.gateway.server.mvc.filter.CircuitBreakerFilterFunctions.circuitBreaker;
import static org.springframework.cloud.gateway.server.mvc.filter.FilterFunctions.setPath;
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;

@Configuration(proxyBeanMethods = false)
public class Routes {

    @Value("${product.service.url}")
    private String productServiceUrl;
    @Value("${order.service.url}")
    private String orderServiceUrl;
    @Value("${inventory.service.url}")
    private String inventoryServiceUrl;
    @Value("${payment.service.url}")
    private String paymnentServiceUrl;
    @Value("${user.service.url}")
    private String userServiceUrl;
    @Value("${notification.service.url}")
    private String notificationServiceUrl;

    @Bean
    public RouterFunction<ServerResponse> productServiceRoute() {
        return GatewayRouterFunctions.route("product_service")
                .route(RequestPredicates.path("/api/products/**").or(RequestPredicates.path("/api/cart/**")), HandlerFunctions.http(productServiceUrl))
                .filter(circuitBreaker("productServiceCircuitBreaker", URI.create("forward:/fallbackRoute")))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> productServiceSwaggerRoute() {
        return GatewayRouterFunctions.route("product_service_swagger")
                .route(RequestPredicates.path("/aggregate/product-service/v3/api-docs"), HandlerFunctions.http(productServiceUrl))
                .filter(circuitBreaker("productServiceSwaggerCircuitBreaker", URI.create("forward:/fallbackRoute")))
                .filter(setPath("/api-docs"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> orderServiceRoute() {
        return GatewayRouterFunctions.route("order_service")
                .route(RequestPredicates.path("/api/orders/**")
                                .and(RequestPredicates.method(HttpMethod.GET)
                                        .or(RequestPredicates.method(HttpMethod.POST))
                                        .or(RequestPredicates.method(HttpMethod.PUT))
                                        .or(RequestPredicates.method(HttpMethod.DELETE))), // Include DELETE here
                        HandlerFunctions.http(orderServiceUrl))
                .filter(circuitBreaker("orderServiceCircuitBreaker", URI.create("forward:/fallbackRoute")))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> orderServiceActuatorRoute() {
        return GatewayRouterFunctions.route("order_service")
                .route(RequestPredicates.path("/order-service/**")
                                .and(RequestPredicates.method(HttpMethod.GET)
                                        .or(RequestPredicates.method(HttpMethod.POST))
                                        .or(RequestPredicates.method(HttpMethod.PUT))
                                        .or(RequestPredicates.method(HttpMethod.DELETE))), // Include DELETE here
                        HandlerFunctions.http(orderServiceUrl))
                .filter(circuitBreaker("orderServiceCircuitBreaker", URI.create("forward:/fallbackRoute")))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> orderServiceSwaggerRoute() {
        return GatewayRouterFunctions.route("order_service_swagger")
                .route(RequestPredicates.path("/aggregate/order-service/v3/api-docs"), HandlerFunctions.http(orderServiceUrl))
                .filter(circuitBreaker("orderServiceSwaggerCircuitBreaker", URI.create("forward:/fallbackRoute")))
                .filter(setPath("/api-docs"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> inventoryServiceRoute() {
        return GatewayRouterFunctions.route("inventory_service")
                .route(RequestPredicates.path("/api/inventory/**")
                                .and(RequestPredicates.method(HttpMethod.GET)
                                        .or(RequestPredicates.method(HttpMethod.POST))
                                        .or(RequestPredicates.method(HttpMethod.PUT))
                                        .or(RequestPredicates.method(HttpMethod.DELETE))), // Include DELETE here
                        HandlerFunctions.http(inventoryServiceUrl))
                .filter(circuitBreaker("inventoryServiceCircuitBreaker", URI.create("forward:/fallbackRoute")))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> inventoryServiceSwaggerRoute() {
        return GatewayRouterFunctions.route("inventory_service_swagger")
                .route(RequestPredicates.path("/aggregate/inventory-service/v3/api-docs"), HandlerFunctions.http(inventoryServiceUrl))
                .filter(circuitBreaker("inventoryServiceSwaggerCircuitBreaker", URI.create("forward:/fallbackRoute")))
                .filter(setPath("/api-docs"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> paymentServiceRoute() {
        return GatewayRouterFunctions.route("payment_service")
                .route(RequestPredicates.path("/api/payments/**")
                                .and(RequestPredicates.method(HttpMethod.GET)
                                        .or(RequestPredicates.method(HttpMethod.POST))
                                        .or(RequestPredicates.method(HttpMethod.PUT))
                                        .or(RequestPredicates.method(HttpMethod.DELETE))), // Include DELETE here
                        HandlerFunctions.http(paymnentServiceUrl))
                .filter(circuitBreaker("inventoryServiceCircuitBreaker", URI.create("forward:/fallbackRoute")))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse>paymentServiceSwaggerRoute() {
        return GatewayRouterFunctions.route("payment_service_swagger")
                .route(RequestPredicates.path("/aggregate/payment-service/v3/api-docs"), HandlerFunctions.http(paymnentServiceUrl))
                .filter(circuitBreaker("inventoryServiceSwaggerCircuitBreaker", URI.create("forward:/fallbackRoute")))
                .filter(setPath("/api-docs"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> userServiceRoute() {
        return GatewayRouterFunctions.route("user_service")
                .route(RequestPredicates.path("/api/users/**")
                                .and(RequestPredicates.method(HttpMethod.GET)
                                        .or(RequestPredicates.method(HttpMethod.POST))
                                        .or(RequestPredicates.method(HttpMethod.PUT))
                                        .or(RequestPredicates.method(HttpMethod.DELETE))), // Include DELETE here
                        HandlerFunctions.http(userServiceUrl))
                .filter(circuitBreaker("userServiceCircuitBreaker", URI.create("forward:/fallbackRoute")))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> userServiceSwaggerRoute() {
        return GatewayRouterFunctions.route("user_service_swagger")
                .route(RequestPredicates.path("/aggregate/user-service/v3/api-docs"), HandlerFunctions.http(userServiceUrl))
                .filter(circuitBreaker("userServiceSwaggerCircuitBreaker", URI.create("forward:/fallbackRoute")))
                .filter(setPath("/api-docs"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> notificationServiceRoute() {
        return GatewayRouterFunctions.route("notification_service")
                .route(RequestPredicates.path("/api/contactMessage/**")
                                .and(RequestPredicates.method(HttpMethod.GET)
                                        .or(RequestPredicates.method(HttpMethod.POST))
                                        .or(RequestPredicates.method(HttpMethod.PUT))
                                        .or(RequestPredicates.method(HttpMethod.DELETE))), // Include DELETE here
                        HandlerFunctions.http(notificationServiceUrl))
                .filter(circuitBreaker("notificationServiceCircuitBreaker", URI.create("forward:/fallbackRoute")))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> notificationServiceSwaggerRoute() {
        return GatewayRouterFunctions.route("notification_service_swagger")
                .route(RequestPredicates.path("/aggregate/notification-service/v3/api-docs"), HandlerFunctions.http(notificationServiceUrl))
                .filter(circuitBreaker("notificationServiceSwaggerCircuitBreaker", URI.create("forward:/fallbackRoute")))
                .filter(setPath("/api-docs"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> fallbackRoute() {
        return route("fallbackRoute")
                .GET("/fallbackRoute", request -> ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE).body("Service Unavailable, please try again later"))
                .build();
    }
}
package pl.xsware.inventory.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI ordersOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Inventory Service API")
                .version("v1")
                .description("Inventory service")
                .contact(new Contact().name("XSware")))
            .servers(List.of(
                new Server().url("http://localhost:8083").description("Local")
            ));
    }
}

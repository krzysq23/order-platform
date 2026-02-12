package pl.xsware.payments.infrastucture.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.servers.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    @Bean
    fun paymentsOpenAPI(): OpenAPI =
        OpenAPI()
            .info(
                Info()
                    .title("Payments Service API")
                    .version("v1")
                    .description("Payments service – consumes PaymentRequested and publishes payment result events")
                    .contact(Contact().name("XSware"))
            )
            .servers(
                listOf(Server().url("http://localhost:8082").description("Local"))
            )
}

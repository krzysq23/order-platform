package pl.xsware.inventory.infrastructure.config;

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
    basePackages = "pl.xsware.inventory.infrastructure.persistance"
)
@EntityScan(
    basePackages = "pl.xsware.inventory.infrastructure.persistance"
)
@EnableJpaAuditing
public class PersistenceConfig {
}

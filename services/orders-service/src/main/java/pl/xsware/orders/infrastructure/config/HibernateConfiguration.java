package pl.xsware.orders.infrastructure.config;

import org.hibernate.cfg.AvailableSettings;
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HibernateConfiguration {

    @Bean
    public HibernatePropertiesCustomizer hibernatePropertiesCustomizer() {
        return hibernateProperties ->
            hibernateProperties.put(
                AvailableSettings.JSON_FORMAT_MAPPER,
                JacksonJsonFormatMapper.class.getName()
            );
    }
}

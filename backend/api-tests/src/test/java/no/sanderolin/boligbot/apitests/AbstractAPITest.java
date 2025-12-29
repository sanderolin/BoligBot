package no.sanderolin.boligbot.apitests;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

public abstract class AbstractAPITest {

    @DynamicPropertySource
    static void setDatasourceProperties(DynamicPropertyRegistry registry) {
        SharedPostgresContainer postgresContainer = SharedPostgresContainer.getInstance();
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
        registry.add("spring.liquibase.change-log", () -> "classpath:db/changelog/db.changelog-master.xml");
        registry.add("spring.liquibase.enabled", () -> true);
    }
}

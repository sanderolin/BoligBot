package no.sanderolin.boligbot.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "no.sanderolin.boligbot")
@EnableJpaRepositories(basePackages = "no.sanderolin.boligbot.dao.repository")
@EntityScan(basePackages = "no.sanderolin.boligbot.dao.model")
@EnableRetry
@EnableScheduling
public class BackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }
}
package no.sanderolin.boligbot.apitests;

import org.testcontainers.containers.PostgreSQLContainer;

public class SharedPostgresContainer extends PostgreSQLContainer<SharedPostgresContainer> {

    private static final String IMAGE = "postgres:16-alpine";
    private static SharedPostgresContainer instance;

    @SuppressWarnings("resource")
    private SharedPostgresContainer() {
        super(IMAGE);
        withDatabaseName("testdb");
        withUsername("testuser");
        withPassword("testpassword");
    }

    public static synchronized SharedPostgresContainer getInstance() {
        if (instance == null) {
            instance = new SharedPostgresContainer();
            instance.start();
        }
        return instance;
    }
}

package si.um.feri.orders.support;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.HashMap;
import java.util.Map;

/**
 * Spins up a disposable PostgreSQL 15 instance for reactive persistence tests.
 */
public class PostgresTestResource implements QuarkusTestResourceLifecycleManager {

    private PostgreSQLContainer<?> postgres;

    @Override
    public Map<String, String> start() {
        postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:15.6-alpine"))
            .withDatabaseName("orders_test")
            .withUsername("orders_test")
            .withPassword("orders_test");
        postgres.start();

        Map<String, String> config = new HashMap<>();
        config.put("quarkus.datasource.username", postgres.getUsername());
        config.put("quarkus.datasource.password", postgres.getPassword());
        config.put("quarkus.datasource.reactive.url", String.format("postgresql://%s:%d/%s",
            postgres.getHost(), postgres.getMappedPort(5432), postgres.getDatabaseName()));
        config.put("quarkus.datasource.jdbc.url", postgres.getJdbcUrl());
        config.put("quarkus.datasource.db-kind", "postgresql");
        config.put("quarkus.flyway.migrate-at-start", "true");
        config.put("quarkus.flyway.schemas", "orders");
        config.put("quarkus.flyway.create-schemas", "true");
        config.put("quarkus.flyway.default-schema", "orders");
        config.put("quarkus.datasource.devservices.enabled", "false");
        return config;
    }

    @Override
    public void stop() {
        if (postgres != null) {
            postgres.stop();
        }
    }
}

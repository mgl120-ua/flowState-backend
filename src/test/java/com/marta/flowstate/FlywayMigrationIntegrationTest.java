package com.marta.flowstate;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Aplica las migraciones sobre un PostgreSQL real y arranca el contexto con ddl-auto=validate,
 * de modo que falla si las entidades JPA y el esquema no coinciden.
 * Solo se ejecuta si existe la variable de entorno FLOWSTATE_TEST_DB_URL (por ejemplo, en CI).
 */
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "FLOWSTATE_TEST_DB_URL", matches = ".+")
class FlywayMigrationIntegrationTest {

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> System.getenv("FLOWSTATE_TEST_DB_URL"));
        registry.add("spring.datasource.username", () -> System.getenv().getOrDefault("FLOWSTATE_TEST_DB_USER", "postgres"));
        registry.add("spring.datasource.password", () -> System.getenv().getOrDefault("FLOWSTATE_TEST_DB_PASSWORD", "postgres"));
        registry.add("jwt.secret", () -> "01234567890123456789012345678901");
    }

    @Autowired
    private Flyway flyway;

    @Test
    void migrationsApplyAndEntitiesMatchSchema() {
        assertNotNull(flyway.info().current());
        assertEquals(0, flyway.info().pending().length);
    }
}

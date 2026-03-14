package org.example;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
class MainApplicationTests {

    /**
     * Test to verify that the Spring Boot application context loads successfully.
     * If the context fails to start, this test will fail automatically.
     */
    @Test
    void contextLoads() {
    }

    /**
     * Test to verify that the main method runs without throwing any exceptions.
     */
    @Test
    void mainMethodRuns() {
        assertDoesNotThrow(() -> MainApplication.main(new String[]{}));
    }
}

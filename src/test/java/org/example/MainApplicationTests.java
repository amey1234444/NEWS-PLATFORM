package org.example;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
class MainApplicationTests {

    @MockBean
    private ChatClient chatClient;

    /**
     * Test to verify that the Spring Boot application context loads successfully.
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

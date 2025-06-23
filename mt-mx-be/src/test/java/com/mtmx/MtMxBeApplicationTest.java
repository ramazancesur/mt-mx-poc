package com.mtmx;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class MtMxBeApplicationTest {

    @Test
    void contextLoads() {
        // This test verifies that the Spring application context loads successfully
        // If there are any configuration issues, this test will fail
    }

    @Test
    void main() {
        // Test that main method can be called without throwing exceptions
        // This is a basic smoke test
        try {
            String[] args = {};
            // We don't actually start the application in test
            // Just verify the class structure is correct
            MtMxBeApplication.class.getDeclaredMethod("main", String[].class);
        } catch (NoSuchMethodException e) {
            throw new AssertionError("Main method not found", e);
        }
    }
}

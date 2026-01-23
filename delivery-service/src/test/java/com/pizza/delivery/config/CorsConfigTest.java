package com.pizza.delivery.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CorsConfig Tests")
class CorsConfigTest {

    private final CorsConfig corsConfig = new CorsConfig();

    @Test
    @DisplayName("Should configure CORS mappings")
    void shouldConfigureCORSMappings() {
        // Given
        TestCorsRegistry registry = new TestCorsRegistry();

        // When
        corsConfig.addCorsMappings(registry);

        // Then
        assertEquals(1, registry.getRegistrations().size());
    }

    @Test
    @DisplayName("Should allow all paths")
    void shouldAllowAllPaths() {
        // Given
        TestCorsRegistry registry = new TestCorsRegistry();

        // When
        corsConfig.addCorsMappings(registry);

        // Then
        assertTrue(registry.getPathPatterns().contains("/**"));
    }

    // Helper class to capture CORS registrations
    private static class TestCorsRegistry extends CorsRegistry {
        private final List<String> pathPatterns = new ArrayList<>();
        private final List<CorsRegistration> registrations = new ArrayList<>();

        @Override
        public CorsRegistration addMapping(String pathPattern) {
            pathPatterns.add(pathPattern);
            CorsRegistration registration = super.addMapping(pathPattern);
            registrations.add(registration);
            return registration;
        }

        public List<String> getPathPatterns() {
            return pathPatterns;
        }

        public List<CorsRegistration> getRegistrations() {
            return registrations;
        }
    }
}

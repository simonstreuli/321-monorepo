package com.pizza.delivery.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OpenApiConfig Tests")
class OpenApiConfigTest {

    private OpenApiConfig openApiConfig;
    private OpenAPI openAPI;

    @BeforeEach
    void setUp() {
        openApiConfig = new OpenApiConfig();
        openAPI = openApiConfig.deliveryServiceOpenAPI();
    }

    @Test
    @DisplayName("Should create OpenAPI bean with correct title")
    void shouldCreateOpenAPIWithCorrectTitle() {
        // Then
        assertNotNull(openAPI);
        Info info = openAPI.getInfo();
        assertNotNull(info);
        assertEquals("Delivery Service API", info.getTitle());
    }

    @Test
    @DisplayName("Should have correct version")
    void shouldHaveCorrectVersion() {
        // Then
        assertEquals("1.0.0", openAPI.getInfo().getVersion());
    }

    @Test
    @DisplayName("Should have description")
    void shouldHaveDescription() {
        // Then
        assertNotNull(openAPI.getInfo().getDescription());
        assertTrue(openAPI.getInfo().getDescription().contains("Delivery Service"));
    }

    @Test
    @DisplayName("Should have contact information")
    void shouldHaveContactInformation() {
        // Then
        assertNotNull(openAPI.getInfo().getContact());
        assertEquals("Pizza Platform Team", openAPI.getInfo().getContact().getName());
    }

    @Test
    @DisplayName("Should have license information")
    void shouldHaveLicenseInformation() {
        // Then
        assertNotNull(openAPI.getInfo().getLicense());
        assertEquals("MIT License", openAPI.getInfo().getLicense().getName());
    }

    @Test
    @DisplayName("Should have servers configured")
    void shouldHaveServersConfigured() {
        // Then
        assertNotNull(openAPI.getServers());
        assertFalse(openAPI.getServers().isEmpty());
    }
}

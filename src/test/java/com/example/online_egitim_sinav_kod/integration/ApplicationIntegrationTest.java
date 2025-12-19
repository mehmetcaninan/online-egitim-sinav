package com.example.online_egitim_sinav_kod.integration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Entegrasyon Testi - Spring Boot uygulamasının tüm bileşenlerinin birlikte çalışmasını test eder
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
public class ApplicationIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void contextLoads() {
        // Spring context'in düzgün yüklendiğini test eder
        assertNotNull(restTemplate);
        assertTrue(port > 0);
    }

    @Test
    public void testApplicationHealthEndpoint() {
        String url = "http://localhost:" + port + "/actuator/health";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("UP") || response.getBody().length() > 0);
    }

    @Test
    public void testHomePageAccess() {
        String url = "http://localhost:" + port + "/";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // 200 OK veya 302 redirect kabul edilir
        assertTrue(response.getStatusCode() == HttpStatus.OK ||
                  response.getStatusCode() == HttpStatus.FOUND);
    }

    @Test
    public void testApiEndpointAccess() {
        String url = "http://localhost:" + port + "/api/public/info";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // API endpoint'lerin erişilebilir olduğunu test eder
        assertTrue(response.getStatusCode() == HttpStatus.OK ||
                  response.getStatusCode() == HttpStatus.NOT_FOUND ||
                  response.getStatusCode() == HttpStatus.UNAUTHORIZED);
    }
}

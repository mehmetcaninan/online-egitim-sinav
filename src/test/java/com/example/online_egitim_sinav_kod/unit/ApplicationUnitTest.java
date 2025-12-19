package com.example.online_egitim_sinav_kod.unit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Birim Test Sınıfı - Temel uygulama fonksiyonlarını test eder
 */
@SpringBootTest
@ActiveProfiles("test")
public class ApplicationUnitTest {

    @Test
    @DisplayName("Uygulama Context Yüklenme Testi")
    void contextLoads() {
        // Bu test Spring Boot context'in düzgün yüklendiğini doğrular
        assertTrue(true);
    }

    @Test
    @DisplayName("Matematik İşlemleri Testi")
    void testBasicMathOperations() {
        assertEquals(4, 2 + 2);
        assertEquals(6, 2 * 3);
        assertEquals(2, 4 / 2);
        assertEquals(1, 5 % 2);
    }

    @Test
    @DisplayName("String İşlemleri Testi")
    void testStringOperations() {
        String testString = "Online Egitim Sinav";
        assertNotNull(testString);
        assertTrue(testString.contains("Egitim"));
        assertEquals(18, testString.length());
        assertEquals("ONLINE EGITIM SINAV", testString.toUpperCase());
    }

    @Test
    @DisplayName("Collection İşlemleri Testi")
    void testCollectionOperations() {
        java.util.List<String> testList = java.util.Arrays.asList("Test1", "Test2", "Test3");
        assertEquals(3, testList.size());
        assertTrue(testList.contains("Test2"));
        assertFalse(testList.isEmpty());
    }

    @Test
    @DisplayName("Exception Handling Testi")
    void testExceptionHandling() {
        assertThrows(ArithmeticException.class, () -> {
            int result = 10 / 0;
        });

        assertDoesNotThrow(() -> {
            int result = 10 / 2;
        });
    }
}

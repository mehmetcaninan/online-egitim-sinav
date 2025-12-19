package com.example.online_egitim_sinav_kod.selenium;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.time.Duration;

/**
 * Tüm Selenium testleri için temel sınıf
 */
public abstract class BaseSeleniumTest {

    protected WebDriver driver;
    protected WebDriverWait wait;
    protected static final String BASE_URL = "http://localhost:8081";

    @BeforeMethod
    public void setUp() {
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless"); // CI/CD için headless mod
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        System.out.println("WebDriver başlatıldı: " + this.getClass().getSimpleName());
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            driver.quit();
            System.out.println("WebDriver kapatıldı: " + this.getClass().getSimpleName());
        }
    }

    protected void navigateToHome() {
        driver.get(BASE_URL);
    }

    protected void waitForPageLoad() {
        try {
            Thread.sleep(2000); // Sayfa yüklenmesi için kısa bekleme
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

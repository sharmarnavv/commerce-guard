package com.commerceguard.monitoring.driver;

import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Component;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class WebDriverManager {

    private static final int MAX_DRIVERS = 20;
    private final BlockingQueue<WebDriver> driverPool;

    public WebDriverManager() {
        this.driverPool = new ArrayBlockingQueue<>(MAX_DRIVERS);
        initializeDriverPool();
    }

    private void initializeDriverPool() {
        WebDriverManager.chromedriver().setup();
        for (int i = 0; i < MAX_DRIVERS; i++) {
            WebDriver driver = createDriver();
            driverPool.offer(driver);
        }
        log.info("Initialized WebDriver pool with {} drivers", MAX_DRIVERS);
    }

    private WebDriver createDriver() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        
        WebDriver driver = new ChromeDriver(options);
        driver.manage().timeouts().pageLoadTimeout(30, TimeUnit.SECONDS);
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        return driver;
    }

    public WebDriver getDriver() throws InterruptedException {
        WebDriver driver = driverPool.poll(30, TimeUnit.SECONDS);
        if (driver == null) {
            throw new RuntimeException("No available WebDriver instances");
        }
        return driver;
    }

    public void releaseDriver(WebDriver driver) {
        if (driver != null) {
            try {
                driver.manage().deleteAllCookies();
                driverPool.offer(driver);
            } catch (Exception e) {
                log.error("Error releasing WebDriver", e);
                replaceDriver(driver);
            }
        }
    }

    private void replaceDriver(WebDriver oldDriver) {
        try {
            oldDriver.quit();
        } catch (Exception e) {
            log.error("Error quitting WebDriver", e);
        }
        
        try {
            WebDriver newDriver = createDriver();
            driverPool.offer(newDriver);
        } catch (Exception e) {
            log.error("Error creating replacement WebDriver", e);
        }
    }

    public void shutdown() {
        driverPool.forEach(driver -> {
            try {
                driver.quit();
            } catch (Exception e) {
                log.error("Error shutting down WebDriver", e);
            }
        });
    }
}

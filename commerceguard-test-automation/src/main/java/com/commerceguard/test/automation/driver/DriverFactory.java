package com.commerceguard.test.automation.driver;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.safari.SafariDriver;
import org.springframework.stereotype.Component;
import io.github.bonigarcia.wdm.WebDriverManager;

@Component
public class DriverFactory {

    public WebDriver createDriver(BrowserType browserType) {
        switch (browserType) {
            case CHROME:
                WebDriverManager.chromedriver().setup();
                return new ChromeDriver();
            case FIREFOX:
                WebDriverManager.firefoxdriver().setup();
                return new FirefoxDriver();
            case EDGE:
                WebDriverManager.edgedriver().setup();
                return new EdgeDriver();
            case SAFARI:
                return new SafariDriver();
            default:
                throw new IllegalArgumentException("Unsupported browser type: " + browserType);
        }
    }
}

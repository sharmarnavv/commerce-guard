package com.commerceguard.monitoring.service;

import com.commerceguard.common.model.Website;
import com.commerceguard.monitoring.driver.WebDriverManager;
import com.commerceguard.monitoring.model.MonitoringResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebDriver;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WebsiteMonitoringServiceTest {

    @Mock
    private WebDriverManager webDriverManager;

    @Mock
    private WebDriver webDriver;

    private WebsiteMonitoringService monitoringService;

    @BeforeEach
    void setUp() throws InterruptedException {
        when(webDriverManager.getDriver()).thenReturn(webDriver);
        monitoringService = new WebsiteMonitoringService(webDriverManager);
    }

    @Test
    void monitorWebsite_ShouldReturnResult() throws ExecutionException, InterruptedException {
        // Arrange
        Website website = new Website();
        website.setId(1L);
        website.setUrl("https://example.com");
        website.setName("Example Website");

        // Act
        CompletableFuture<MonitoringResult> futureResult = monitoringService.monitorWebsite(website);
        MonitoringResult result = futureResult.get();

        // Assert
        assertNotNull(result);
        assertEquals(website.getId(), result.getWebsiteId());
        assertEquals("UP", result.getStatus());
    }

    @Test
    void monitorWebsite_WhenError_ShouldReturnFailureResult() throws ExecutionException, InterruptedException {
        // Arrange
        Website website = new Website();
        website.setId(1L);
        website.setUrl("https://example.com");
        
        when(webDriver.get(any())).thenThrow(new RuntimeException("Connection failed"));

        // Act
        CompletableFuture<MonitoringResult> futureResult = monitoringService.monitorWebsite(website);
        MonitoringResult result = futureResult.get();

        // Assert
        assertNotNull(result);
        assertEquals(website.getId(), result.getWebsiteId());
        assertEquals("DOWN", result.getStatus());
        assertNotNull(result.getError());
    }
}

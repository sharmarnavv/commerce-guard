package com.commerceguard.monitoring.service;

import com.commerceguard.common.model.Website;
import com.commerceguard.monitoring.driver.WebDriverManager;
import com.commerceguard.monitoring.model.MonitoringData;
import com.commerceguard.monitoring.model.MonitoringResult;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class WebsiteMonitoringService {
    
    private final ExecutorService monitoringPool;
    private final ScheduledExecutorService scheduler;
    private final BlockingQueue<MonitoringData> dataQueue;
    private final WebDriverManager webDriverManager;

    @Autowired
    public WebsiteMonitoringService(WebDriverManager webDriverManager) {
        this.monitoringPool = Executors.newFixedThreadPool(20);
        this.scheduler = Executors.newScheduledThreadPool(5);
        this.dataQueue = new LinkedBlockingQueue<>(10000);
        this.webDriverManager = webDriverManager;
        
        // Start data processing
        startDataConsumers();
    }

    @Async
    @CircuitBreaker(name = "monitoring", fallbackMethod = "fallbackMonitoring")
    @Retry(name = "monitoring")
    public CompletableFuture<MonitoringResult> monitorWebsite(Website website) {
        return CompletableFuture.supplyAsync(() -> {
            WebDriver driver = null;
            try {
                driver = webDriverManager.getDriver();
                MonitoringResult result = performMonitoring(driver, website);
                dataQueue.offer(new MonitoringData(website, result));
                return result;
            } catch (Exception e) {
                log.error("Error monitoring website: " + website.getUrl(), e);
                throw e;
            } finally {
                if (driver != null) {
                    webDriverManager.releaseDriver(driver);
                }
            }
        }, monitoringPool);
    }

    private MonitoringResult performMonitoring(WebDriver driver, Website website) {
        long startTime = System.currentTimeMillis();
        driver.get(website.getUrl());
        long loadTime = System.currentTimeMillis() - startTime;

        return MonitoringResult.builder()
            .websiteId(website.getId())
            .timestamp(System.currentTimeMillis())
            .status("UP")
            .responseTime(loadTime)
            .build();
    }

    private void startDataConsumers() {
        int numConsumers = 5;
        for (int i = 0; i < numConsumers; i++) {
            scheduler.scheduleWithFixedDelay(this::processDataQueue, 0, 1, TimeUnit.SECONDS);
        }
    }

    private void processDataQueue() {
        try {
            MonitoringData data = dataQueue.poll(1, TimeUnit.SECONDS);
            if (data != null) {
                // Process and store monitoring data
                log.info("Processing monitoring data for website: {}", data.getWebsite().getUrl());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Data processing interrupted", e);
        }
    }

    private CompletableFuture<MonitoringResult> fallbackMonitoring(Website website, Throwable t) {
        log.error("Fallback monitoring for website: " + website.getUrl(), t);
        return CompletableFuture.completedFuture(
            MonitoringResult.builder()
                .websiteId(website.getId())
                .timestamp(System.currentTimeMillis())
                .status("DOWN")
                .error(t.getMessage())
                .build()
        );
    }
}

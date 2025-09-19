package com.commerceguard.test.automation.suite;

import com.commerceguard.test.automation.model.TestCase;
import com.commerceguard.test.automation.model.TestResult;
import com.commerceguard.test.automation.model.PerformanceMetrics;
import com.commerceguard.test.automation.model.ComparisonResult;
import com.commerceguard.test.automation.driver.BrowserType;
import com.commerceguard.test.automation.driver.DriverFactory;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.springframework.stereotype.Component;
import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.Screenshot;
import ru.yandex.qatools.ashot.comparison.ImageDiff;
import ru.yandex.qatools.ashot.comparison.ImageDiffer;

import javax.imageio.ImageIO;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ECommerceTestSuite {
    
    private final ExecutorService testExecutor = Executors.newFixedThreadPool(10);
    private final DriverFactory driverFactory;

    public ECommerceTestSuite(DriverFactory driverFactory) {
        this.driverFactory = driverFactory;
    }

    public List<TestResult> runParallelTests(List<TestCase> testCases) {
        List<CompletableFuture<TestResult>> futures = testCases.stream()
            .map(this::executeTestAsync)
            .collect(Collectors.toList());

        return futures.stream()
            .map(CompletableFuture::join)
            .collect(Collectors.toList());
    }

    private CompletableFuture<TestResult> executeTestAsync(TestCase testCase) {
        return CompletableFuture.supplyAsync(() -> {
            WebDriver driver = null;
            try {
                driver = driverFactory.createDriver(testCase.getBrowserType());
                return executeTest(driver, testCase);
            } catch (Exception e) {
                log.error("Error executing test: " + testCase.getName(), e);
                return TestResult.builder()
                    .testCase(testCase)
                    .status("FAILED")
                    .error(e.getMessage())
                    .build();
            } finally {
                if (driver != null) {
                    driver.quit();
                }
            }
        }, testExecutor);
    }

    public TestResult executeCrossBrowserTest(TestCase testCase) {
        List<TestResult> results = new ArrayList<>();
        
        for (BrowserType browserType : BrowserType.values()) {
            WebDriver driver = null;
            try {
                driver = driverFactory.createDriver(browserType);
                TestCase browserSpecificTest = testCase.toBuilder()
                    .browserType(browserType)
                    .build();
                results.add(executeTest(driver, browserSpecificTest));
            } catch (Exception e) {
                log.error("Error in cross-browser test for " + browserType, e);
                results.add(TestResult.builder()
                    .testCase(testCase)
                    .status("FAILED")
                    .error(e.getMessage())
                    .build());
            } finally {
                if (driver != null) {
                    driver.quit();
                }
            }
        }

        return aggregateResults(testCase, results);
    }

    public PerformanceMetrics measurePagePerformance(String url) {
        WebDriver driver = null;
        try {
            driver = driverFactory.createDriver(BrowserType.CHROME);
            long startTime = System.currentTimeMillis();
            driver.get(url);
            long loadTime = System.currentTimeMillis() - startTime;

            return PerformanceMetrics.builder()
                .url(url)
                .pageLoadTime(loadTime)
                .timestamp(System.currentTimeMillis())
                .build();
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    public ComparisonResult performVisualRegression(String baseline, String current) {
        try {
            Screenshot baselineScreenshot = new AShot().takeScreenshot(
                driverFactory.createDriver(BrowserType.CHROME), 
                new File(baseline)
            );
            
            Screenshot currentScreenshot = new AShot().takeScreenshot(
                driverFactory.createDriver(BrowserType.CHROME), 
                new File(current)
            );

            ImageDiff diff = new ImageDiffer().makeDiff(baselineScreenshot, currentScreenshot);
            
            return ComparisonResult.builder()
                .hasDifferences(diff.hasDiff())
                .diffPercentage(calculateDiffPercentage(diff))
                .diffAreas(diff.getDiffSize())
                .build();
        } catch (Exception e) {
            log.error("Error performing visual regression", e);
            throw new RuntimeException("Visual regression failed", e);
        }
    }

    private TestResult executeTest(WebDriver driver, TestCase testCase) {
        long startTime = System.currentTimeMillis();
        
        try {
            testCase.getSteps().forEach(step -> executeTestStep(driver, step));
            
            return TestResult.builder()
                .testCase(testCase)
                .status("PASSED")
                .executionTime(System.currentTimeMillis() - startTime)
                .build();
        } catch (Exception e) {
            log.error("Test execution failed: " + testCase.getName(), e);
            return TestResult.builder()
                .testCase(testCase)
                .status("FAILED")
                .error(e.getMessage())
                .executionTime(System.currentTimeMillis() - startTime)
                .build();
        }
    }

    private void executeTestStep(WebDriver driver, String step) {
        // Implementation of test step execution
        log.info("Executing test step: {}", step);
    }

    private TestResult aggregateResults(TestCase testCase, List<TestResult> results) {
        boolean allPassed = results.stream()
            .allMatch(result -> "PASSED".equals(result.getStatus()));
        
        return TestResult.builder()
            .testCase(testCase)
            .status(allPassed ? "PASSED" : "FAILED")
            .subResults(results)
            .build();
    }

    private double calculateDiffPercentage(ImageDiff diff) {
        // Implementation of diff percentage calculation
        return (double) diff.getDiffSize() / (diff.getMarkedImage().getWidth() * diff.getMarkedImage().getHeight()) * 100;
    }
}

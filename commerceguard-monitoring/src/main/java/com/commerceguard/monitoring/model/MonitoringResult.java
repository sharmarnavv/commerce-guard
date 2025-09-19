package com.commerceguard.monitoring.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MonitoringResult {
    private Long websiteId;
    private long timestamp;
    private String status;
    private long responseTime;
    private String error;
    private String screenshot;
    private String pageSource;
}

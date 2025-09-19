package com.commerceguard.monitoring.model;

import com.commerceguard.common.model.Website;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonitoringData {
    private Website website;
    private MonitoringResult result;
}

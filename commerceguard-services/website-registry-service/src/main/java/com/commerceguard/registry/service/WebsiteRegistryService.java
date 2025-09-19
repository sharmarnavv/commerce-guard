package com.commerceguard.registry.service;

import com.commerceguard.common.exception.CommerceGuardException;
import com.commerceguard.common.model.Website;
import com.commerceguard.registry.repository.WebsiteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WebsiteRegistryService {

    private final WebsiteRepository websiteRepository;

    @Transactional
    public Website registerWebsite(Website website) {
        validateWebsite(website);
        return websiteRepository.save(website);
    }

    @Transactional(readOnly = true)
    public List<Website> getAllWebsites() {
        return websiteRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Website getWebsite(Long id) {
        return websiteRepository.findById(id)
            .orElseThrow(() -> new CommerceGuardException(
                "WEBSITE_NOT_FOUND",
                "Website not found with id: " + id,
                404
            ));
    }

    @Transactional
    public Website updateWebsite(Long id, Website website) {
        Website existingWebsite = getWebsite(id);
        updateWebsiteFields(existingWebsite, website);
        return websiteRepository.save(existingWebsite);
    }

    @Transactional
    public void deleteWebsite(Long id) {
        if (!websiteRepository.existsById(id)) {
            throw new CommerceGuardException(
                "WEBSITE_NOT_FOUND",
                "Website not found with id: " + id,
                404
            );
        }
        websiteRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Website> getActiveWebsites() {
        return websiteRepository.findByActiveTrue();
    }

    private void validateWebsite(Website website) {
        if (website.getMonitoringInterval() < 30) {
            throw new CommerceGuardException(
                "INVALID_INTERVAL",
                "Monitoring interval must be at least 30 seconds",
                400
            );
        }
    }

    private void updateWebsiteFields(Website existing, Website updated) {
        existing.setName(updated.getName());
        existing.setUrl(updated.getUrl());
        existing.setMonitoringInterval(updated.getMonitoringInterval());
        existing.setActive(updated.isActive());
        existing.setRegion(updated.getRegion());
        existing.setCredentials(updated.getCredentials());
        existing.setMonitoringParameters(updated.getMonitoringParameters());
    }
}

package com.commerceguard.registry.controller;

import com.commerceguard.common.model.Website;
import com.commerceguard.registry.service.WebsiteRegistryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/websites")
@RequiredArgsConstructor
public class WebsiteRegistryController {

    private final WebsiteRegistryService websiteRegistryService;

    @PostMapping
    public ResponseEntity<Website> registerWebsite(@Valid @RequestBody Website website) {
        return ResponseEntity.ok(websiteRegistryService.registerWebsite(website));
    }

    @GetMapping
    public ResponseEntity<List<Website>> getAllWebsites() {
        return ResponseEntity.ok(websiteRegistryService.getAllWebsites());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Website> getWebsite(@PathVariable Long id) {
        return ResponseEntity.ok(websiteRegistryService.getWebsite(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Website> updateWebsite(
            @PathVariable Long id,
            @Valid @RequestBody Website website) {
        return ResponseEntity.ok(websiteRegistryService.updateWebsite(id, website));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWebsite(@PathVariable Long id) {
        websiteRegistryService.deleteWebsite(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/active")
    public ResponseEntity<List<Website>> getActiveWebsites() {
        return ResponseEntity.ok(websiteRegistryService.getActiveWebsites());
    }
}

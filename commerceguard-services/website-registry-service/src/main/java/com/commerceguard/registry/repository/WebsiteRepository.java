package com.commerceguard.registry.repository;

import com.commerceguard.common.model.Website;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WebsiteRepository extends JpaRepository<Website, Long> {
    List<Website> findByActiveTrue();
}

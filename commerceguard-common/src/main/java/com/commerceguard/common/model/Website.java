package com.commerceguard.common.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "websites")
public class Website extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String url;

    @Column(nullable = false)
    private int monitoringInterval; // in seconds

    @Column(nullable = false)
    private boolean active;

    @Column
    private String region;

    @Column
    private String credentials; // encrypted if needed

    @Column
    private String monitoringParameters; // JSON string of additional parameters
}

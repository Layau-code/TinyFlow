package com.layor.tinyflow.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "click_event", indexes = {
        @Index(name = "idx_code_ts", columnList = "short_code, ts")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClickEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "short_code", nullable = false)
    private String shortCode;

    @Column(name = "ts", nullable = false)
    private LocalDateTime ts;

    @Column(name = "referer")
    private String referer;

    @Column(name = "ua")
    private String ua;

    @Column(name = "ip")
    private String ip;

    @Column(name = "source_host")
    private String sourceHost;

    @Column(name = "device_type")
    private String deviceType;

    @Column(name = "city")
    private String city;

    @Column(name = "country")
    private String country;
}
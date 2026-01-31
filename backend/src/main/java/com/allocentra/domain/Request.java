package com.allocentra.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a resource allocation request
 */
@Entity
@Table(name = "requests", indexes = {
    @Index(name = "idx_cycle_id", columnList = "cycle_id"),
    @Index(name = "idx_cycle_status", columnList = "cycle_id, status")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Request {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cycle_id", nullable = false)
    private AllocationCycle cycle;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(length = 2000)
    private String justification;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResourceCategory category;

    @Column(precision = 19, scale = 2)
    private BigDecimal amountRequested;

    @Column(precision = 19, scale = 2)
    private BigDecimal minimumViableAllocation;

    private String resourceType;

    @Column(precision = 19, scale = 2)
    private BigDecimal quantityRequested;

    @Column(precision = 19, scale = 2)
    private BigDecimal minimumViableQuantity;

    @Column(nullable = false)
    @Builder.Default
    private Integer priority = 3;

    @Column(nullable = false)
    private LocalDate urgencyDeadline;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Impact impact = Impact.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Risk risk = Risk.LOW;

    @Column(nullable = false)
    @Builder.Default
    private Integer strategic = 3;

    @ElementCollection
    @CollectionTable(name = "request_dependencies", 
        joinColumns = @JoinColumn(name = "request_id"))
    @Column(name = "dependency_id")
    @Builder.Default
    private List<String> dependencies = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RequestStatus status = RequestStatus.PENDING;

    private Double score;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private String createdBy;

    private LocalDate startDate;
    private LocalDate endDate;

    public enum Impact {
        LOW(1),
        MEDIUM(3),
        HIGH(4),
        CRITICAL(5);

        private final int value;

        Impact(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public enum Risk {
        LOW(1),
        OPERATIONAL(3),
        SAFETY(5),
        LEGAL(5);

        private final int value;

        Risk(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public enum RequestStatus {
        PENDING,    // Not yet allocated
        APPROVED,   // Fully approved
        PARTIAL,    // Partially funded
        DEFERRED,   // Postponed (dependency or other reason)
        DENIED      // Not funded
    }
}

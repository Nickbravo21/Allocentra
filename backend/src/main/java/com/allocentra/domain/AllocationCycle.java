package com.allocentra.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents an allocation cycle (monthly, quarterly, mission window)
 * Contains budget pools, resource pools, and requests for a specific time period
 */
@Entity
@Table(name = "allocation_cycles")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AllocationCycle {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private CycleStatus status = CycleStatus.DRAFT;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @OneToMany(mappedBy = "cycle", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<BudgetPool> budgetPools = new ArrayList<>();

    @OneToMany(mappedBy = "cycle", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ResourcePool> resourcePools = new ArrayList<>();

    @OneToMany(mappedBy = "cycle", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Request> requests = new ArrayList<>();

    @Column(nullable = false)
    @Builder.Default
    private boolean allowPartialAllocations = true;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;

    private String createdBy;

    public enum CycleStatus {
        DRAFT,      // Being created
        ACTIVE,     // Ready for requests
        CLOSED,     // No longer accepting requests
        ARCHIVED    // Historical record
    }

    // Helper methods
    public void addBudgetPool(BudgetPool pool) {
        budgetPools.add(pool);
        pool.setCycle(this);
    }

    public void addResourcePool(ResourcePool pool) {
        resourcePools.add(pool);
        pool.setCycle(this);
    }

    public void addRequest(Request request) {
        requests.add(request);
        request.setCycle(this);
    }
}

package com.allocentra.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Represents a pool of non-monetary resources (vehicles, equipment, personnel hours)
 */
@Entity
@Table(name = "resource_pools")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourcePool {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cycle_id", nullable = false)
    private AllocationCycle cycle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResourceCategory category;

    @Column(nullable = false)
    private String resourceType;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalQuantity;

    @Column(nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal allocatedQuantity = BigDecimal.ZERO;

    @Column(nullable = false)
    @Builder.Default
    private String unit = "COUNT";

    @Column(precision = 19, scale = 2)
    private BigDecimal availableHours;

    @Column(nullable = false)
    @Builder.Default
    private boolean exclusive = false;

    public BigDecimal getRemainingQuantity() {
        return totalQuantity.subtract(allocatedQuantity);
    }

    public void allocate(BigDecimal quantity) {
        this.allocatedQuantity = this.allocatedQuantity.add(quantity);
    }

    public void deallocate(BigDecimal quantity) {
        this.allocatedQuantity = this.allocatedQuantity.subtract(quantity);
    }
}

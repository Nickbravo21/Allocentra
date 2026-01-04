package com.allocentra.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Represents a pool of budget for a specific category
 */
@Entity
@Table(name = "budget_pools")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetPool {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cycle_id", nullable = false)
    private AllocationCycle cycle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResourceCategory category;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal allocatedAmount = BigDecimal.ZERO;

    @Column(length = 3)
    @Builder.Default
    private String currency = "USD";

    public BigDecimal getRemainingAmount() {
        return totalAmount.subtract(allocatedAmount);
    }

    public void allocate(BigDecimal amount) {
        this.allocatedAmount = this.allocatedAmount.add(amount);
    }

    public void deallocate(BigDecimal amount) {
        this.allocatedAmount = this.allocatedAmount.subtract(amount);
    }
}

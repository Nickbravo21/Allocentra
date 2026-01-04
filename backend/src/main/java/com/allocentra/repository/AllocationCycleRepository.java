package com.allocentra.repository;

import com.allocentra.domain.AllocationCycle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AllocationCycleRepository extends JpaRepository<AllocationCycle, String> {
    
    List<AllocationCycle> findByStatus(AllocationCycle.CycleStatus status);
    
    @Query("SELECT c FROM AllocationCycle c LEFT JOIN FETCH c.budgetPools LEFT JOIN FETCH c.resourcePools WHERE c.id = :id")
    AllocationCycle findByIdWithPools(String id);
}

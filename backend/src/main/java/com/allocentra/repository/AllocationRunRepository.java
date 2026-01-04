package com.allocentra.repository;

import com.allocentra.domain.AllocationRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AllocationRunRepository extends JpaRepository<AllocationRun, String> {
    
    List<AllocationRun> findByCycleIdOrderByCreatedAtDesc(String cycleId);
    
    List<AllocationRun> findByStatusOrderByCreatedAtDesc(AllocationRun.RunStatus status);
    
    @Query("SELECT r FROM AllocationRun r LEFT JOIN FETCH r.results WHERE r.id = :id")
    AllocationRun findByIdWithResults(String id);
}

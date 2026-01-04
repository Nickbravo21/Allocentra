package com.allocentra.repository;

import com.allocentra.domain.Request;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequestRepository extends JpaRepository<Request, String> {
    
    Page<Request> findByCycleId(String cycleId, Pageable pageable);
    
    Page<Request> findByCycleIdAndStatus(String cycleId, Request.RequestStatus status, Pageable pageable);
    
    Page<Request> findByCycleIdAndCategory(String cycleId, com.allocentra.domain.ResourceCategory category, Pageable pageable);
    
    List<Request> findByCycleIdOrderByScoreDesc(String cycleId);
}

package com.peoplebase.api.leave.repository;

import com.peoplebase.api.leave.entity.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    @Query("select request from LeaveRequest request where request.workflowRequest.targetEmployee.id = :requesterId order by request.createdAt desc")
    List<LeaveRequest> findByRequesterIdOrderByCreatedAtDesc(@Param("requesterId") Long requesterId);
}

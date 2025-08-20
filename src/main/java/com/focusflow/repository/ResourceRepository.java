package com.focusflow.repository;

import com.focusflow.model.Resource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ResourceRepository extends JpaRepository<Resource, Long> {
    
    List<Resource> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    List<Resource> findByProjectIdOrderByCreatedAtDesc(Long projectId);
    
    List<Resource> findByUserIdAndProjectIdOrderByCreatedAtDesc(Long userId, Long projectId);
    
    @Query("SELECT r FROM Resource r WHERE r.user.id = :userId AND r.fileType = :fileType ORDER BY r.createdAt DESC")
    List<Resource> findByUserIdAndFileTypeOrderByCreatedAtDesc(@Param("userId") Long userId, @Param("fileType") String fileType);
    
    @Query("SELECT COUNT(r) FROM Resource r WHERE r.project.id = :projectId")
    Long countByProjectId(@Param("projectId") Long projectId);
}
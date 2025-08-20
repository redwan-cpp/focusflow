package com.focusflow.repository;

import com.focusflow.model.Note;
import com.focusflow.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NoteRepository extends JpaRepository<Note, Long> {
    
    List<Note> findByUserOrderByIsPinnedDescUpdatedAtDesc(User user);
    
    List<Note> findByUserAndIsPinnedTrueOrderByUpdatedAtDesc(User user);
    
    List<Note> findByUserAndIsFavoriteTrueOrderByUpdatedAtDesc(User user);
    
    List<Note> findByUserAndCategoryIdOrderByUpdatedAtDesc(User user, Long categoryId);

    List<Note> findByUserAndProjectIdOrderByUpdatedAtDesc(User user, Long projectId);
    
    @Query("SELECT n FROM Note n WHERE n.user = :user AND (LOWER(n.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(n.content) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Note> findByUserAndSearchTerm(@Param("user") User user, @Param("searchTerm") String searchTerm);
    
    @Query("SELECT n FROM Note n WHERE n.user = :user AND :tag MEMBER OF n.tags")
    List<Note> findByUserAndTag(@Param("user") User user, @Param("tag") String tag);
    
    Page<Note> findByUser(User user, Pageable pageable);
    
    List<Note> findByUserAndNoteTypeOrderByUpdatedAtDesc(User user, String noteType);
}
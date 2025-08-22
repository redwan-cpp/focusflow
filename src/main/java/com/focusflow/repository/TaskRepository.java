package com.focusflow.repository;

import com.focusflow.model.Task;
import com.focusflow.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.time.LocalDateTime;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findAllByUser(User user);
    List<Task> findByUserAndCompletedFalseOrderByCreatedAtDesc(User user);
    List<Task> findByUserAndCompletedTrueOrderByCompletedAtDesc(User user);

    long countByUserAndDueDateBetween(User user, LocalDateTime start, LocalDateTime end);
    long countByUserAndCompletedTrueAndDueDateBetween(User user, LocalDateTime start, LocalDateTime end);

    long countByUserAndCompletedFalse(User user);
}
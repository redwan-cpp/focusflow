package com.focusflow.repository;

import com.focusflow.model.Task;
import com.focusflow.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findAllByUser(User user);
}
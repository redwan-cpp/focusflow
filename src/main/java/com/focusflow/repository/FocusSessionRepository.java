package com.focusflow.repository;

import com.focusflow.model.FocusSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FocusSessionRepository extends JpaRepository<FocusSession, Long> {
}
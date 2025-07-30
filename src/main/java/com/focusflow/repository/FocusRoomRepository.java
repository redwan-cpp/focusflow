package com.focusflow.repository;

import com.focusflow.model.FocusRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FocusRoomRepository extends JpaRepository<FocusRoom, Long> {
}
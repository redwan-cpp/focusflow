// src/main/java/com/focusflow/repository/UserRepository.java
package com.focusflow.repository;

import com.focusflow.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}

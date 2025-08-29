// src/main/java/com/focusflow/repository/UserRepository.java
package com.focusflow.repository;

import com.focusflow.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    
    @Query("SELECT u FROM User u WHERE LOWER(u.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<User> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(@Param("query") String query);
    
    @Query("SELECT u FROM User u WHERE LOWER(u.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<User> findByNameContainingIgnoreCase(@Param("name") String name);
    
    // Alternative method for more specific searches
    @Query("SELECT u FROM User u WHERE LOWER(u.name) LIKE LOWER(CONCAT('%', :nameQuery, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :emailQuery, '%'))")
    List<User> findByNameOrEmailContaining(@Param("nameQuery") String nameQuery, @Param("emailQuery") String emailQuery);
}

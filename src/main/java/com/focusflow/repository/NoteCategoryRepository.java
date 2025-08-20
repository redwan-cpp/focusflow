package com.focusflow.repository;

import com.focusflow.model.NoteCategory;
import com.focusflow.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoteCategoryRepository extends JpaRepository<NoteCategory, Long> {
    
    List<NoteCategory> findByUserOrderByName(User user);
    
    NoteCategory findByNameAndUser(String name, User user);
    
    boolean existsByNameAndUser(String name, User user);
}

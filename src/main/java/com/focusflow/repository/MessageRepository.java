package com.focusflow.repository;

import com.focusflow.model.Message;
import com.focusflow.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    
    @Query("SELECT m FROM Message m WHERE (m.sender = :user1 AND m.receiver = :user2) OR (m.sender = :user2 AND m.receiver = :user1) ORDER BY m.createdAt ASC")
    List<Message> findConversationBetweenUsers(@Param("user1") User user1, @Param("user2") User user2);
    
    @Query("SELECT m FROM Message m WHERE m.receiver = :user AND m.isRead = false ORDER BY m.createdAt DESC")
    List<Message> findUnreadMessagesForUser(@Param("user") User user);
    
    @Query("SELECT COUNT(m) FROM Message m WHERE m.receiver = :user AND m.isRead = false")
    long countUnreadMessagesForUser(@Param("user") User user);
    
    @Query("SELECT m FROM Message m WHERE m.receiver = :user ORDER BY m.createdAt DESC")
    List<Message> findRecentMessagesForUser(@Param("user") User user);
}

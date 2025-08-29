package com.focusflow.repository;

import com.focusflow.model.FriendRequest;
import com.focusflow.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {
    
    @Query("SELECT fr FROM FriendRequest fr WHERE (fr.sender = :user OR fr.receiver = :user) AND fr.status = 'PENDING'")
    List<FriendRequest> findPendingRequestsByUser(@Param("user") User user);
    
    @Query("SELECT fr FROM FriendRequest fr WHERE fr.sender = :sender AND fr.receiver = :receiver AND fr.status = 'PENDING'")
    Optional<FriendRequest> findPendingRequest(@Param("sender") User sender, @Param("receiver") User receiver);
    
    @Query("SELECT fr FROM FriendRequest fr WHERE fr.receiver = :user AND fr.status = 'PENDING'")
    List<FriendRequest> findReceivedPendingRequests(@Param("user") User user);
    
    @Query("SELECT fr FROM FriendRequest fr WHERE fr.sender = :user AND fr.status = 'PENDING'")
    List<FriendRequest> findSentPendingRequests(@Param("user") User user);
    
    @Query("SELECT fr FROM FriendRequest fr WHERE (fr.sender = :user1 AND fr.receiver = :user2) OR (fr.sender = :user2 AND fr.receiver = :user1)")
    List<FriendRequest> findRequestsBetweenUsers(@Param("user1") User user1, @Param("user2") User user2);
}

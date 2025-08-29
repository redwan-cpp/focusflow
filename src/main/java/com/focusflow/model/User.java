// src/main/java/com/focusflow/model/User.java
package com.focusflow.model;

import jakarta.persistence.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "created_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Timestamp createdAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Task> tasks = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Project> projects = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Note> notes = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Resource> resources = new ArrayList<>();

    @ManyToMany(mappedBy = "participants")
    private Set<FocusRoom> focusRooms = new HashSet<>();

    // Friend relationships
    @ManyToMany
    @JoinTable(
        name = "user_friends",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "friend_id")
    )
    private Set<User> friends = new HashSet<>();

    @ManyToMany(mappedBy = "friends")
    private Set<User> friendOf = new HashSet<>();

    // Friend requests
    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FriendRequest> sentFriendRequests = new ArrayList<>();

    @OneToMany(mappedBy = "receiver", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FriendRequest> receivedFriendRequests = new ArrayList<>();

    // Messages
    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Message> sentMessages = new ArrayList<>();

    @OneToMany(mappedBy = "receiver", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Message> receivedMessages = new ArrayList<>();

    // Transient fields for UI
    @Transient
    private boolean isFriend = false;

    @Transient
    private boolean hasPendingRequest = false;

    @Transient
    private long unreadMessageCount = 0;

    public User() {
        // set current time on creation
        this.createdAt = new Timestamp(System.currentTimeMillis());
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    // Friend methods
    public Set<User> getFriends() {
        return friends;
    }

    public void setFriends(Set<User> friends) {
        this.friends = friends;
    }

    public Set<User> getFriendOf() {
        return friendOf;
    }

    public void setFriendOf(Set<User> friendOf) {
        this.friendOf = friendOf;
    }

    public List<FriendRequest> getSentFriendRequests() {
        return sentFriendRequests;
    }

    public void setSentFriendRequests(List<FriendRequest> sentFriendRequests) {
        this.sentFriendRequests = sentFriendRequests;
    }

    public List<FriendRequest> getReceivedFriendRequests() {
        return receivedFriendRequests;
    }

    public void setReceivedFriendRequests(List<FriendRequest> receivedFriendRequests) {
        this.receivedFriendRequests = receivedFriendRequests;
    }

    public List<Message> getSentMessages() {
        return sentMessages;
    }

    public void setSentMessages(List<Message> sentMessages) {
        this.sentMessages = sentMessages;
    }

    public List<Message> getReceivedMessages() {
        return receivedMessages;
    }

    public void setReceivedMessages(List<Message> receivedMessages) {
        this.receivedMessages = receivedMessages;
    }

    // Transient field getters and setters
    public boolean isFriend() {
        return isFriend;
    }

    public void setIsFriend(boolean isFriend) {
        this.isFriend = isFriend;
    }

    public boolean isHasPendingRequest() {
        return hasPendingRequest;
    }

    public void setHasPendingRequest(boolean hasPendingRequest) {
        this.hasPendingRequest = hasPendingRequest;
    }

    public long getUnreadMessageCount() {
        return unreadMessageCount;
    }

    public void setUnreadMessageCount(long unreadMessageCount) {
        this.unreadMessageCount = unreadMessageCount;
    }

    // Helper methods
    public void addFriend(User friend) {
        this.friends.add(friend);
        friend.getFriends().add(this);
    }

    public void removeFriend(User friend) {
        this.friends.remove(friend);
        friend.getFriends().remove(this);
    }

    public boolean isFriendWith(User user) {
        return this.friends.contains(user);
    }
}

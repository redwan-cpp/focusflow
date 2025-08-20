package com.focusflow.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.*;

@Entity
public class FocusRoom {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean isActive;

    @ManyToMany
    @JoinTable(
        name = "focusroom_participants",
        joinColumns = @JoinColumn(name = "focusroom_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> participants = new HashSet<>();

    @OneToMany(mappedBy = "focusRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FocusSession> sessions = new ArrayList<>();

    // Getters, setters, constructors
    public FocusRoom() {}

    public FocusRoom(Long id, String name, String description, LocalDateTime startTime, LocalDateTime endTime, boolean isActive, Set<User> participants, List<FocusSession> sessions) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.startTime = startTime;
        this.endTime = endTime;
        this.isActive = isActive;
        this.participants = participants;
        this.sessions = sessions;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public Set<User> getParticipants() { return participants; }
    public void setParticipants(Set<User> participants) { this.participants = participants; }

    public List<FocusSession> getSessions() { return sessions; }
    public void setSessions(List<FocusSession> sessions) { this.sessions = sessions; }
}
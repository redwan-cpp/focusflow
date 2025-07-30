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
}
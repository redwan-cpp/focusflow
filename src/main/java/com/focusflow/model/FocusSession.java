package com.focusflow.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class FocusSession {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "focusroom_id")
    private FocusRoom focusRoom;

    private LocalDateTime joinTime;
    private LocalDateTime leaveTime;

    // Getters, setters, constructors
}
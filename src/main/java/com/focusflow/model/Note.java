package com.focusflow.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Note {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    @Column(length = 5000)
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // Getters, setters, constructors
}
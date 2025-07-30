package com.focusflow.model;

import jakarta.persistence.*;

@Entity
public class Resource {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String url;
    private String type; // e.g., "LINK", "FILE", etc.
    private String description;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // Getters, setters, constructors
}
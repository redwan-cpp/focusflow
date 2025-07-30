package com.focusflow.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Statistic {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private LocalDate date;
    private int focusTimeMinutes;
    private int tasksCompleted;
    private int pomodoros;

    // Getters, setters, constructors
}
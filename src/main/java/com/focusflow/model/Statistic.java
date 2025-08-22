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

    public Statistic() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public int getFocusTimeMinutes() {
        return focusTimeMinutes;
    }

    public void setFocusTimeMinutes(int focusTimeMinutes) {
        this.focusTimeMinutes = focusTimeMinutes;
    }

    public int getTasksCompleted() {
        return tasksCompleted;
    }

    public void setTasksCompleted(int tasksCompleted) {
        this.tasksCompleted = tasksCompleted;
    }

    public int getPomodoros() {
        return pomodoros;
    }

    public void setPomodoros(int pomodoros) {
        this.pomodoros = pomodoros;
    }
}
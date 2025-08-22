package com.focusflow.repository;

import com.focusflow.model.Statistic;
import com.focusflow.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface StatisticRepository extends JpaRepository<Statistic, Long> {
    Optional<Statistic> findByUserAndDate(User user, LocalDate date);

    @Query("SELECT COALESCE(SUM(s.focusTimeMinutes), 0) FROM Statistic s WHERE s.user = :user AND s.date BETWEEN :start AND :end")
    int sumFocusTimeMinutesBetween(@Param("user") User user,
                                   @Param("start") LocalDate start,
                                   @Param("end") LocalDate end);

    @Query("SELECT COALESCE(SUM(s.tasksCompleted), 0) FROM Statistic s WHERE s.user = :user AND s.date BETWEEN :start AND :end")
    int sumTasksCompletedBetween(@Param("user") User user,
                                 @Param("start") LocalDate start,
                                 @Param("end") LocalDate end);
}
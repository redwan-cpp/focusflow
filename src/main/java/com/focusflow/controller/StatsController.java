package com.focusflow.controller;

import com.focusflow.model.Statistic;
import com.focusflow.model.User;
import com.focusflow.repository.StatisticRepository;
import com.focusflow.repository.TaskRepository;
import com.focusflow.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.*;
import java.util.Map;

@Controller
public class StatsController {

    private final UserRepository userRepository;
    private final StatisticRepository statisticRepository;
    private final TaskRepository taskRepository;

    public StatsController(UserRepository userRepository,
                           StatisticRepository statisticRepository,
                           TaskRepository taskRepository) {
        this.userRepository = userRepository;
        this.statisticRepository = statisticRepository;
        this.taskRepository = taskRepository;
    }

    @GetMapping("/stats")
    public String statsPage(Model model, Principal principal) {
        User user = userRepository.findByEmail(principal.getName()).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", user);

        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(DayOfWeek.MONDAY);
        LocalDate weekEnd = weekStart.plusDays(6);

        int weekFocusMinutes = statisticRepository.sumFocusTimeMinutesBetween(user, weekStart, weekEnd);
        int weekTasksCompleted = statisticRepository.sumTasksCompletedBetween(user, weekStart, weekEnd);
        int weekHours = weekFocusMinutes / 60;
        int weekMinutesRemainder = weekFocusMinutes % 60;

        LocalDateTime weekStartDateTime = weekStart.atStartOfDay();
        LocalDateTime weekEndDateTime = weekEnd.atTime(LocalTime.MAX);
        long weekTasksTotal = taskRepository.countByUserAndDueDateBetween(user, weekStartDateTime, weekEndDateTime);
        long weekTasksCompletedCount = taskRepository.countByUserAndCompletedTrueAndDueDateBetween(user, weekStartDateTime, weekEndDateTime);

        int productivity = weekTasksTotal == 0 ? 0 : (int) Math.round((weekTasksCompletedCount * 100.0) / weekTasksTotal);

        // streak: day counts when focus >= 30 minutes per day
        int streak = calculateStreak(user, today);

        model.addAttribute("weekFocusMinutes", weekFocusMinutes);
        model.addAttribute("weekTasksCompleted", weekTasksCompleted);
        model.addAttribute("productivity", productivity);
        model.addAttribute("streak", streak);
        model.addAttribute("weekHours", weekHours);
        model.addAttribute("weekMinutesRemainder", weekMinutesRemainder);

        return "stats";
    }

    private int calculateStreak(User user, LocalDate today) {
        int streak = 0;
        LocalDate cursor = today;
        while (true) {
            int minutes = statisticRepository.findByUserAndDate(user, cursor)
                    .map(Statistic::getFocusTimeMinutes)
                    .orElse(0);
            if (minutes >= 30) {
                streak++;
                cursor = cursor.minusDays(1);
            } else {
                break;
            }
        }
        return streak;
    }

    @PostMapping("/api/stats/focus")
    @ResponseBody
    public ResponseEntity<?> addFocusTime(@RequestBody Map<String, Object> body, Principal principal) {
        User user = userRepository.findByEmail(principal.getName()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        int elapsedSeconds = ((Number) body.getOrDefault("elapsedSeconds", 0)).intValue();
        if (elapsedSeconds <= 0) {
            return ResponseEntity.badRequest().body("elapsedSeconds must be > 0");
        }
        boolean completed = false;
        Object completedObj = body.get("completed");
        if (completedObj instanceof Boolean) {
            completed = (Boolean) completedObj;
        }

        LocalDate today = LocalDate.now();
        Statistic stat = statisticRepository.findByUserAndDate(user, today).orElseGet(() -> {
            Statistic s = new Statistic();
            s.setUser(user);
            s.setDate(today);
            s.setFocusTimeMinutes(0);
            s.setTasksCompleted(0);
            s.setPomodoros(0);
            return s;
        });
        int addedMinutes = Math.floorDiv(elapsedSeconds, 60);
        stat.setFocusTimeMinutes(stat.getFocusTimeMinutes() + addedMinutes);
        if (completed) {
            stat.setPomodoros(stat.getPomodoros() + 1);
        }
        statisticRepository.save(stat);

        // Prepare lightweight summary for UI
        LocalDate weekStart = today.with(DayOfWeek.MONDAY);
        LocalDate weekEnd = weekStart.plusDays(6);
        int weekFocusMinutes = statisticRepository.sumFocusTimeMinutesBetween(user, weekStart, weekEnd);
        int todayMinutes = stat.getFocusTimeMinutes();
        int todayPomodoros = stat.getPomodoros();

        return ResponseEntity.ok(Map.of(
                "todayMinutes", todayMinutes,
                "weekMinutes", weekFocusMinutes,
                "todayPomodoros", todayPomodoros
        ));
    }

    @GetMapping("/api/stats/today")
    @ResponseBody
    public ResponseEntity<?> getTodayStats(Principal principal) {
        User user = userRepository.findByEmail(principal.getName()).orElse(null);
        if (user == null) return ResponseEntity.status(401).build();
        LocalDate today = LocalDate.now();
        Statistic stat = statisticRepository.findByUserAndDate(user, today)
                .orElseGet(() -> {
                    Statistic s = new Statistic();
                    s.setUser(user);
                    s.setDate(today);
                    s.setFocusTimeMinutes(0);
                    s.setTasksCompleted(0);
                    s.setPomodoros(0);
                    return s;
                });
        // productivity today based on due today (optional simple metric)
        return ResponseEntity.ok(Map.of(
                "todayMinutes", stat.getFocusTimeMinutes(),
                "todayTasksCompleted", stat.getTasksCompleted(),
                "todayPomodoros", stat.getPomodoros()
        ));
    }

    @GetMapping("/api/stats/month")
    @ResponseBody
    public ResponseEntity<?> getMonthStats(@RequestParam int year, @RequestParam int month, Principal principal) {
        User user = userRepository.findByEmail(principal.getName()).orElse(null);
        if (user == null) return ResponseEntity.status(401).build();
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        // Build an array mapping day->minutes
        int days = start.lengthOfMonth();
        int[] minutes = new int[days];
        for (int d = 1; d <= days; d++) {
            LocalDate date = LocalDate.of(year, month, d);
            int m = statisticRepository.findByUserAndDate(user, date).map(Statistic::getFocusTimeMinutes).orElse(0);
            minutes[d - 1] = m;
        }
        return ResponseEntity.ok(Map.of("minutes", minutes));
    }
}



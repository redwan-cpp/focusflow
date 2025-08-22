# FocusFlow

A personal productivity and study manager built with Spring Boot, featuring tasks, projects, notes, resources, real-time focus rooms, and insightful statistics powered by a persistent, editable focus timer.

## Features

- Authentication with email/password (BCrypt)
- Tasks management with priorities, due dates, per-project association
  - Unfinished vs Finished sections
  - Sorting: unfinished by created time (newest first), finished by completion time (newest first)
- Projects, Notes (tags), Resources library with uploads
- Focus Rooms with WebSocket (STOMP)
- Focus Timer on dashboard
  - Double-click to edit MM:SS
  - Persists across navigation (localStorage + absolute end timestamp)
  - Sends elapsed time to backend on pause/complete
- Statistics
  - Weekly focus minutes, tasks completed, productivity %, daily streak (>= 30 min/day)
  - Seconds floored to minutes

## Tech Stack

- Java 17, Spring Boot 3.5
- Spring MVC, Spring Security, Spring Data JPA (MySQL)
- Thymeleaf templates
- WebSocket (STOMP) with SockJS
- Bootstrap 5

## Setup

1. Configure database in `src/main/resources/application.properties`:
```
spring.datasource.url=jdbc:mysql://localhost:3306/focusflow
spring.datasource.username=YOUR_DB_USER
spring.datasource.password=YOUR_DB_PASSWORD
spring.jpa.hibernate.ddl-auto=update
```
2. Run the app:
```
./mvnw spring-boot:run
```
Visit `http://localhost:8080`.

## Key Endpoints

- Auth: `/register`, `/login`, `/logout`
- App: `/dashboard`, `/tasks`, `/projects`, `/notes`, `/resources`, `/focus-room`, `/stats`
- API (CSRF ignored): `POST /api/stats/focus` with `{ "elapsedSeconds": number }`

## Timer & Stats Notes

- Timer persists across page navigation and resumes based on absolute end time.
- Elapsed seconds are floored to minutes before persisting to `Statistic.focusTimeMinutes`.
- Task toggle updates `Statistic.tasksCompleted` for the day.
- Productivity: completed-due-this-week / total-due-this-week.
- Streak counts consecutive days with >= 30 minutes focus.

## Notable Files

- `DashboardController` — dynamic card counts
- `StatsController` — `/stats` and focus time API
- `Task` — `createdAt`, `completedAt` for ordering
- `tasks.html` — Unfinished/Finished lists with required sorting
- `dashboard.html` — persistent, editable timer
- `stats.html` — safe time formatting, dynamic metrics

## Build

```
./mvnw clean package
```

## License

Add a license if open-sourcing.

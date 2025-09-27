# FocusFlow

A comprehensive personal productivity and study manager built with Spring Boot, featuring tasks, projects, notes, resources, real-time focus rooms, friend connections, messaging, and insightful statistics powered by a persistent, editable focus timer.

## ✨ Features

- **🔐 Authentication** - Secure email/password login with BCrypt encryption
- **📋 Task Management** - Create, organize, and track tasks with priorities and due dates
  - Smart sorting: unfinished by creation time, finished by completion time
  - Project association and progress tracking
- **📁 Project Organization** - Manage multiple projects with deadlines and status tracking
- **📝 Rich Notes System** - Create notes with categories, tags, and multiple formats
  - Support for TEXT, CHECKLIST, CODE, DRAWING, AUDIO, and IMAGE notes
  - Color coding and pinning functionality
- **📚 Resource Library** - Upload and organize files, links, and documents
- **👥 Social Features** - Connect with friends, send friend requests, and direct messaging
- **🏠 Focus Rooms** - Real-time collaborative focus sessions with WebSocket support
- **⏱️ Smart Focus Timer** - Persistent timer with customizable duration
  - Double-click to edit time, persists across navigation
  - Automatic time tracking and statistics
- **📊 Productivity Analytics** - Comprehensive statistics and insights
  - Focus time tracking, task completion rates, productivity percentages
  - Daily streaks and weekly summaries

## 🛠️ Tech Stack

- **Backend**: Java 17, Spring Boot 3.5
- **Security**: Spring Security with BCrypt
- **Database**: MySQL 8+ with Spring Data JPA
- **Frontend**: Thymeleaf templates with Bootstrap 5
- **Real-time**: WebSocket (STOMP) with SockJS
- **Build Tool**: Maven

## 🚀 Quick Start

### Prerequisites
- **Java 17+** ([Download here](https://adoptium.net/))
- **MySQL 8.0+** ([Download here](https://dev.mysql.com/downloads/))
- **Git** ([Download here](https://git-scm.com/))

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/YOUR_USERNAME/focusflow.git
   cd focusflow
   ```

2. **Set up the database**
   ```bash
   # Create database
   mysql -u root -p
   CREATE DATABASE focusflow;
   
   # Import schema
   mysql -u root -p focusflow < focusflow_schema.sql
   ```

3. **Configure the application**
   ```bash
   # Copy and edit configuration
   cp src/main/resources/application.properties.example src/main/resources/application.properties
   ```
   
   Update `src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/focusflow
   spring.datasource.username=your_mysql_username
   spring.datasource.password=your_mysql_password
   ```

4. **Run the application**
   ```bash
   # Using Maven wrapper
   ./mvnw spring-boot:run
   
   # Or build and run JAR
   ./mvnw clean package
   java -jar target/focusflow-0.0.1-SNAPSHOT.jar
   ```

5. **Access the application**
   - Open your browser and go to `http://localhost:8080`
   - Register a new account or login

### Alternative Setup with Docker

```bash
# Using Docker Compose (if available)
docker-compose up -d mysql
./mvnw spring-boot:run
```

## 📖 Documentation

### Key Endpoints
- **Authentication**: `/register`, `/login`, `/logout`
- **Main App**: `/dashboard`, `/tasks`, `/projects`, `/notes`, `/resources`, `/focus-room`, `/stats`, `/friends`, `/messages`
- **API**: `POST /api/stats/focus` with `{ "elapsedSeconds": number }` (CSRF ignored)

### Database Schema
The application uses the provided `focusflow_schema.sql` file to create all necessary tables and relationships. Key entities include:
- **Users** with authentication and profile management
- **Tasks & Projects** for productivity tracking
- **Notes & Categories** for rich content management
- **Resources** for file and link organization
- **Focus Rooms & Sessions** for collaborative work
- **Messages & Friends** for social features
- **Statistics** for analytics and insights

### Configuration Options
- **Development**: `spring.jpa.hibernate.ddl-auto=update` (auto-creates tables)
- **Production**: `spring.jpa.hibernate.ddl-auto=validate` (validates existing schema)
- **File Uploads**: Configurable max file size (default: 50MB)
- **Security**: BCrypt password encryption with Spring Security

## 🔧 Development

### Project Structure
```
src/main/java/com/focusflow/
├── config/          # Security, WebSocket, File Upload configuration
├── controller/      # REST endpoints and web controllers
├── model/          # JPA entities and data models
├── repository/     # Data access layer
└── service/        # Business logic layer

src/main/resources/
├── templates/      # Thymeleaf HTML templates
├── static/         # CSS, JS, images
└── application.properties.example  # Configuration template
```

### Building and Running
```bash
# Development
./mvnw spring-boot:run

# Production build
./mvnw clean package
java -jar target/focusflow-0.0.1-SNAPSHOT.jar

# Run tests
./mvnw test
```

### Key Features Implementation
- **Timer Persistence**: Uses localStorage + absolute timestamps
- **Statistics**: Focus time floored to minutes, task completion tracking
- **Real-time Features**: WebSocket for focus room collaboration
- **File Management**: Secure upload handling with type validation
- **Social Features**: Friend requests, messaging, and user discovery

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📋 Requirements

- **Java 17+**
- **MySQL 8.0+**
- **Maven 3.6+** (or use included Maven wrapper)

## 🐛 Troubleshooting

### Common Issues
- **Database Connection**: Ensure MySQL is running and credentials are correct
- **Port Conflicts**: Change `server.port` in application.properties if 8080 is in use
- **File Uploads**: Check `uploads/` directory permissions
- **WebSocket Issues**: Ensure no proxy is blocking WebSocket connections

### Getting Help
- Check the [Issues](https://github.com/YOUR_USERNAME/focusflow/issues) page
- Review the application logs for detailed error messages
- Ensure all prerequisites are properly installed

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- Built with Spring Boot and Spring Security
- UI components from Bootstrap 5
- Real-time features powered by WebSocket and SockJS

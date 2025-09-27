-- FocusFlow Database Schema
-- MySQL 8.0+ Compatible
-- Created for FocusFlow project setup

-- Create database (uncomment if you need to create the database)
-- CREATE DATABASE IF NOT EXISTS focusflow;
-- USE focusflow;

-- =====================================================
-- CORE USER MANAGEMENT TABLES
-- =====================================================

-- Users table
CREATE TABLE IF NOT EXISTS user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    avatar_url VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_email (email),
    INDEX idx_user_name (name)
);

-- =====================================================
-- FRIENDSHIP AND MESSAGING TABLES
-- =====================================================

-- Friend requests table
CREATE TABLE IF NOT EXISTS friend_request (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sender_id BIGINT NOT NULL,
    receiver_id BIGINT NOT NULL,
    status ENUM('PENDING', 'ACCEPTED', 'REJECTED') NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (sender_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (receiver_id) REFERENCES user(id) ON DELETE CASCADE,
    UNIQUE KEY unique_friend_request (sender_id, receiver_id),
    INDEX idx_friend_request_sender (sender_id),
    INDEX idx_friend_request_receiver (receiver_id),
    INDEX idx_friend_request_status (status)
);

-- User friends many-to-many relationship
CREATE TABLE IF NOT EXISTS user_friends (
    user_id BIGINT NOT NULL,
    friend_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, friend_id),
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (friend_id) REFERENCES user(id) ON DELETE CASCADE
);

-- Messages table
CREATE TABLE IF NOT EXISTS message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sender_id BIGINT NOT NULL,
    receiver_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sender_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (receiver_id) REFERENCES user(id) ON DELETE CASCADE,
    INDEX idx_message_sender (sender_id),
    INDEX idx_message_receiver (receiver_id),
    INDEX idx_message_created_at (created_at),
    INDEX idx_message_unread (receiver_id, is_read)
);

-- =====================================================
-- PROJECT AND TASK MANAGEMENT TABLES
-- =====================================================

-- Projects table
CREATE TABLE IF NOT EXISTS project (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    deadline DATETIME,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    user_id BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    INDEX idx_project_user (user_id),
    INDEX idx_project_status (status)
);

-- Tasks table
CREATE TABLE IF NOT EXISTS task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    due_date DATETIME,
    priority VARCHAR(20) DEFAULT 'MEDIUM',
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL,
    completed_at DATETIME,
    user_id BIGINT NOT NULL,
    project_id BIGINT,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE SET NULL,
    INDEX idx_task_user (user_id),
    INDEX idx_task_project (project_id),
    INDEX idx_task_completed (completed),
    INDEX idx_task_due_date (due_date)
);

-- =====================================================
-- NOTES AND CATEGORIES TABLES
-- =====================================================

-- Note categories table
CREATE TABLE IF NOT EXISTS note_category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    color VARCHAR(20),
    icon VARCHAR(50),
    created_at DATETIME NOT NULL,
    user_id BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    INDEX idx_note_category_user (user_id)
);

-- Notes table
CREATE TABLE IF NOT EXISTS note (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    is_pinned BOOLEAN NOT NULL DEFAULT FALSE,
    is_favorite BOOLEAN NOT NULL DEFAULT FALSE,
    color_tag VARCHAR(20),
    note_type ENUM('TEXT', 'CHECKLIST', 'CODE', 'DRAWING', 'AUDIO', 'IMAGE') NOT NULL DEFAULT 'TEXT',
    font_size INT DEFAULT 14,
    font_family VARCHAR(50) DEFAULT 'Inter',
    user_id BIGINT NOT NULL,
    project_id BIGINT,
    category_id BIGINT,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE SET NULL,
    FOREIGN KEY (category_id) REFERENCES note_category(id) ON DELETE SET NULL,
    INDEX idx_note_user (user_id),
    INDEX idx_note_project (project_id),
    INDEX idx_note_category (category_id),
    INDEX idx_note_type (note_type),
    INDEX idx_note_pinned (is_pinned),
    INDEX idx_note_favorite (is_favorite)
);

-- Note tags collection table
CREATE TABLE IF NOT EXISTS note_tags (
    note_id BIGINT NOT NULL,
    tag VARCHAR(100) NOT NULL,
    PRIMARY KEY (note_id, tag),
    FOREIGN KEY (note_id) REFERENCES note(id) ON DELETE CASCADE,
    INDEX idx_note_tags_tag (tag)
);

-- =====================================================
-- RESOURCES TABLE
-- =====================================================

-- Resources table
CREATE TABLE IF NOT EXISTS resource (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255),
    url VARCHAR(1000),
    file_name VARCHAR(255),
    file_path VARCHAR(500),
    file_type VARCHAR(20),
    description TEXT,
    file_size VARCHAR(20),
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    user_id BIGINT NOT NULL,
    project_id BIGINT,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE SET NULL,
    INDEX idx_resource_user (user_id),
    INDEX idx_resource_project (project_id),
    INDEX idx_resource_type (file_type)
);

-- =====================================================
-- FOCUS ROOMS AND SESSIONS TABLES
-- =====================================================

-- Focus rooms table
CREATE TABLE IF NOT EXISTS focusroom (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    start_time DATETIME,
    end_time DATETIME,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    INDEX idx_focusroom_active (is_active),
    INDEX idx_focusroom_times (start_time, end_time)
);

-- Focus room participants many-to-many relationship
CREATE TABLE IF NOT EXISTS focusroom_participants (
    focusroom_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (focusroom_id, user_id),
    FOREIGN KEY (focusroom_id) REFERENCES focusroom(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
);

-- Focus sessions table
CREATE TABLE IF NOT EXISTS focus_session (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    focusroom_id BIGINT,
    join_time DATETIME,
    leave_time DATETIME,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (focusroom_id) REFERENCES focusroom(id) ON DELETE SET NULL,
    INDEX idx_focus_session_user (user_id),
    INDEX idx_focus_session_room (focusroom_id),
    INDEX idx_focus_session_times (join_time, leave_time)
);

-- =====================================================
-- STATISTICS TABLE
-- =====================================================

-- Statistics table
CREATE TABLE IF NOT EXISTS statistic (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    date DATE NOT NULL,
    focus_time_minutes INT NOT NULL DEFAULT 0,
    tasks_completed INT NOT NULL DEFAULT 0,
    pomodoros INT NOT NULL DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_date (user_id, date),
    INDEX idx_statistic_user (user_id),
    INDEX idx_statistic_date (date)
);

-- =====================================================
-- SAMPLE DATA (Optional - for testing)
-- =====================================================

-- Insert sample user categories if needed
-- INSERT INTO note_category (name, description, color, icon, created_at, user_id) VALUES
-- ('Work', 'Work-related notes', '#3B82F6', 'briefcase', NOW(), 1),
-- ('Personal', 'Personal notes and reminders', '#10B981', 'user', NOW(), 1),
-- ('Learning', 'Study notes and resources', '#F59E0B', 'book', NOW(), 1);

-- =====================================================
-- USEFUL QUERIES FOR DEVELOPMENT
-- =====================================================

-- View all tables
-- SHOW TABLES;

-- Check table structures
-- DESCRIBE user;
-- DESCRIBE project;
-- DESCRIBE task;

-- Sample queries for testing
-- SELECT * FROM user;
-- SELECT * FROM project WHERE user_id = 1;
-- SELECT t.*, p.name as project_name FROM task t LEFT JOIN project p ON t.project_id = p.id WHERE t.user_id = 1;

-- =====================================================
-- NOTES FOR SETUP
-- =====================================================

/*
Setup Instructions:
1. Make sure MySQL 8.0+ is installed and running
2. Update application.properties with your database credentials:
   - spring.datasource.url=jdbc:mysql://localhost:3306/focusflow
   - spring.datasource.username=your_username
   - spring.datasource.password=your_password
3. Run this schema file: mysql -u your_username -p focusflow < focusflow_schema.sql
4. Set spring.jpa.hibernate.ddl-auto=validate in production (or update for development)
5. The application will handle any missing indexes or optimizations automatically

Security Notes:
- Change default passwords
- Use environment variables for database credentials in production
- Consider using SSL for database connections in production
- Regular backups recommended

Performance Notes:
- Indexes are created for commonly queried fields
- Consider additional indexes based on your specific query patterns
- Monitor query performance and add indexes as needed
*/

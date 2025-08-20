package com.focusflow.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Resource {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String url;
    private String fileName; // For uploaded files
    private String fileType; // PDF, DOC, IMAGE, LINK
    private String description;
    private String fileSize; // For uploaded files
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Helper method to get file icon based on type
    public String getFileIcon() {
        switch (fileType != null ? fileType.toUpperCase() : "") {
            case "PDF":
                return "bi-file-earmark-pdf";
            case "DOC":
            case "DOCX":
                return "bi-file-earmark-word";
            case "IMAGE":
            case "JPG":
            case "JPEG":
            case "PNG":
            case "GIF":
                return "bi-file-earmark-image";
            case "LINK":
                return "bi-link-45deg";
            default:
                return "bi-file-earmark";
        }
    }

    // Helper method to get file type color
    public String getFileTypeColor() {
        switch (fileType != null ? fileType.toUpperCase() : "") {
            case "PDF":
                return "#dc3545";
            case "DOC":
            case "DOCX":
                return "#0d6efd";
            case "IMAGE":
            case "JPG":
            case "JPEG":
            case "PNG":
            case "GIF":
                return "#198754";
            case "LINK":
                return "#fd7e14";
            default:
                return "#6c757d";
        }
    }
}
package com.example.online_egitim_sinav_kod.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "video_resources")
public class VideoResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private String videoUrl;

    @Enumerated(EnumType.STRING)
    private VideoType videoType = VideoType.OTHER;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

    @ManyToOne
    @JoinColumn(name = "uploaded_by")
    private User uploadedBy;

    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();

    // Video Type Enum
    public enum VideoType {
        LECTURE,
        TUTORIAL,
        DEMONSTRATION,
        EXERCISE,
        OTHER
    }

    // Constructors
    public VideoResource() {}

    public VideoResource(String title, String videoUrl, Course course, User uploadedBy) {
        this.title = title;
        this.videoUrl = videoUrl;
        this.course = course;
        this.uploadedBy = uploadedBy;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

    public VideoType getVideoType() { return videoType; }
    public void setVideoType(VideoType videoType) { this.videoType = videoType; }

    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }

    public User getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(User uploadedBy) { this.uploadedBy = uploadedBy; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }
}

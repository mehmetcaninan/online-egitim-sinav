package com.example.online_egitim_sinav_kod.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "notes")
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String content;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    private Instant createdAt = Instant.now();

    // Constructors
    public Note() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }
    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    // Eksik metodlar eklendi
    public User getAuthor() { return createdBy; }
    public void setAuthor(User author) { this.createdBy = author; }
    public void setResource(Object resource) { /* Temporary implementation */ }
}

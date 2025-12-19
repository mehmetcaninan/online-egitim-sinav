package com.example.online_egitim_sinav_kod.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;

    @ManyToOne
    @JoinColumn(name = "teacher_id", nullable = false)
    @JsonIgnoreProperties({"password", "courses", "enrollments", "createdExams"})
    private User teacher;

    private Instant createdAt = Instant.now();
    private boolean active = true;

    @JsonIgnore
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Exam> exams = new ArrayList<>();

    // Constructors
    public Course() {}

    public Course(String title, String description, User teacher) {
        this.title = title;
        this.description = description;
        this.teacher = teacher;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public User getTeacher() { return teacher; }
    public void setTeacher(User teacher) { this.teacher = teacher; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public List<Exam> getExams() { return exams; }
    public void setExams(List<Exam> exams) { this.exams = exams; }

    // Temporary methods for compatibility - these should be replaced with proper entity relationships
    public List<Object> getResources() { return new ArrayList<>(); }
    public void setResources(List<Object> resources) { /* Implementation needed */ }
}

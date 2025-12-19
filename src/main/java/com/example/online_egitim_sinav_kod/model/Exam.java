package com.example.online_egitim_sinav_kod.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "exams")
public class Exam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;

    @ManyToOne
    @JoinColumn(name = "course_id")
    @JsonIgnoreProperties({"exams"})
    private Course course;

    @ManyToOne
    @JoinColumn(name = "created_by")
    @JsonIgnoreProperties({"password", "courses", "enrollments", "createdExams"})
    private User createdBy;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer duration; // in minutes
    private Boolean isActive = true;
    private Boolean isTest = false; // Eksik alan eklendi
    private Instant createdAt = Instant.now();

    @OneToMany(mappedBy = "exam", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Question> questions = new ArrayList<>();

    // Constructors
    public Exam() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }
    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    // Eksik metodlar eklendi
    public Boolean getIsTest() { return isTest; }
    public Boolean isTest() { return isTest; }
    public void setTest(Boolean isTest) { this.isTest = isTest; }
    public void setIsTest(Boolean isTest) { this.isTest = isTest; }

    public List<Question> getQuestions() { return questions; }
    public void setQuestions(List<Question> questions) { this.questions = questions; }
}

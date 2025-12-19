package com.example.online_egitim_sinav_kod.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "exam_submissions")
public class ExamSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "exam_id", nullable = false)
    @JsonIgnoreProperties({"questions", "course", "createdBy"})
    private Exam exam;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    @JsonIgnoreProperties({"password", "courses", "enrollments"})
    private User student;

    @Column(columnDefinition = "TEXT")
    private String answers; // JSON string: {"1": "answer1", "2": "answer2", ...}

    private Double score; // Otomatik hesaplanan puan
    private Double maxScore; // Maksimum puan
    private Instant submittedAt = Instant.now();
    private Boolean isGraded = false;

    // Constructors
    public ExamSubmission() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Exam getExam() { return exam; }
    public void setExam(Exam exam) { this.exam = exam; }

    public User getStudent() { return student; }
    public void setStudent(User student) { this.student = student; }

    public String getAnswers() { return answers; }
    public void setAnswers(String answers) { this.answers = answers; }

    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }

    public Double getMaxScore() { return maxScore; }
    public void setMaxScore(Double maxScore) { this.maxScore = maxScore; }

    public Instant getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(Instant submittedAt) { this.submittedAt = submittedAt; }

    public Boolean getIsGraded() { return isGraded; }
    public void setIsGraded(Boolean isGraded) { this.isGraded = isGraded; }
}


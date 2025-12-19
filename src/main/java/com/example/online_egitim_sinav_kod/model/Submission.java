package com.example.online_egitim_sinav_kod.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "submissions")
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "exam_id")
    private Exam exam;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String answers; // JSON format of answers
    private Integer score;
    private LocalDateTime submittedAt;
    private Instant createdAt = Instant.now();

    // Eksik alanlar eklendi
    private String answerText;
    private String selectedOptions;

    // Constructors
    public Submission() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Exam getExam() { return exam; }
    public void setExam(Exam exam) { this.exam = exam; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getAnswers() { return answers; }
    public void setAnswers(String answers) { this.answers = answers; }
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    // Eksik metodlar eklendi
    public String getAnswerText() { return answerText; }
    public void setAnswerText(String answerText) { this.answerText = answerText; }
    public String getSelectedOptions() { return selectedOptions; }
    public void setSelectedOptions(String selectedOptions) { this.selectedOptions = selectedOptions; }
    public User getStudent() { return user; }
    public void setStudent(User student) { this.user = student; }
}

package com.example.online_egitim_sinav_kod.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "questions")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public enum QuestionType {
        MULTIPLE_CHOICE,
        CLASSIC
    }

    // Ana soru metni
    private String text;

    // Çoktan seçmeli seçenekler (JSON string olarak saklanan)
    private String options;

    // Doğru cevap index'i (çoktan seçmeli için)
    private Integer correctOptionIndex;

    // Klasik sorular için anahtar metin
    private String answerKey;

    // Soru puanı
    private Double points = 1.0;

    @Enumerated(EnumType.STRING)
    private QuestionType type = QuestionType.MULTIPLE_CHOICE;

    @ManyToOne
    @JoinColumn(name = "exam_id")
    @JsonIgnoreProperties({"questions", "course", "createdBy"})
    private Exam exam;

    private Instant createdAt = Instant.now();

    // Constructors
    public Question() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getOptions() { return options; }
    public void setOptions(String options) { this.options = options; }

    public Integer getCorrectOptionIndex() { return correctOptionIndex; }
    public void setCorrectOptionIndex(Integer correctOptionIndex) { this.correctOptionIndex = correctOptionIndex; }

    public String getAnswerKey() { return answerKey; }
    public void setAnswerKey(String answerKey) { this.answerKey = answerKey; }

    public Double getPoints() { return points; }
    public void setPoints(Double points) { this.points = points; }

    public QuestionType getType() { return type; }
    public void setType(QuestionType type) { this.type = type; }

    // Frontend için questionType alias
    @JsonProperty("questionType")
    public QuestionType getQuestionType() { return type; }

    public Exam getExam() { return exam; }
    public void setExam(Exam exam) { this.exam = exam; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}

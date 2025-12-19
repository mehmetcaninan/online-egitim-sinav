package com.example.online_egitim_sinav_kod.controller;

import com.example.online_egitim_sinav_kod.model.Exam;
import com.example.online_egitim_sinav_kod.model.ExamSubmission;
import com.example.online_egitim_sinav_kod.model.Question;
import com.example.online_egitim_sinav_kod.model.User;
import com.example.online_egitim_sinav_kod.repository.ExamRepository;
import com.example.online_egitim_sinav_kod.repository.ExamSubmissionRepository;
import com.example.online_egitim_sinav_kod.repository.QuestionRepository;
import com.example.online_egitim_sinav_kod.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/exam-submissions")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"}, allowedHeaders = "*")
public class ExamSubmissionController {

    private final ExamSubmissionRepository submissionRepository;
    private final ExamRepository examRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public ExamSubmissionController(ExamSubmissionRepository submissionRepository,
                                   ExamRepository examRepository,
                                   QuestionRepository questionRepository,
                                   UserRepository userRepository,
                                   ObjectMapper objectMapper) {
        this.submissionRepository = submissionRepository;
        this.examRepository = examRepository;
        this.questionRepository = questionRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    public static class SubmitExamRequest {
        public Long examId;
        public Long studentId;
        public Map<String, String> answers; // questionId -> answer
    }

    // DTO for response
    public static class SubmissionDTO {
        public Long id;
        public Long examId;
        public String examTitle;
        public Long studentId;
        public String studentUsername;
        public String studentFullName;
        public Double score;
        public Double maxScore;
        public Double percentage;
        public Boolean isGraded;
        public String submittedAt;
    }

    private SubmissionDTO toDTO(ExamSubmission submission) {
        SubmissionDTO dto = new SubmissionDTO();
        dto.id = submission.getId();
        dto.examId = submission.getExam().getId();
        dto.examTitle = submission.getExam().getTitle();
        dto.studentId = submission.getStudent().getId();
        dto.studentUsername = submission.getStudent().getUsername();
        dto.studentFullName = submission.getStudent().getFullName();
        dto.score = submission.getScore();
        dto.maxScore = submission.getMaxScore();
        dto.percentage = submission.getMaxScore() > 0 ? (submission.getScore() / submission.getMaxScore() * 100) : 0;
        dto.isGraded = submission.getIsGraded();
        dto.submittedAt = submission.getSubmittedAt() != null ? submission.getSubmittedAt().toString() : null;
        return dto;
    }

    // Sınav gönderimi ve otomatik puanlama
    @PostMapping
    public ResponseEntity<?> submitExam(@RequestBody SubmitExamRequest request) {
        try {
            System.out.println("ExamSubmission: Sınav gönderimi alındı - ExamId: " + request.examId + ", StudentId: " + request.studentId);

            // Daha önce gönderilmiş mi kontrol et
            Optional<ExamSubmission> existingSubmission = submissionRepository.findByExamIdAndStudentId(request.examId, request.studentId);
            if (existingSubmission.isPresent()) {
                System.out.println("ExamSubmission: Bu sınav zaten gönderilmiş!");
                return ResponseEntity.badRequest().body(Map.of("error", "Bu sınavı zaten gönderdiniz"));
            }

            // Sınavı bul
            Optional<Exam> examOpt = examRepository.findById(request.examId);
            if (examOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            Exam exam = examOpt.get();

            // Öğrenciyi bul
            Optional<User> studentOpt = userRepository.findById(request.studentId);
            if (studentOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            User student = studentOpt.get();

            // Soruları al
            List<Question> questions = questionRepository.findByExamIdOrderByIdAsc(request.examId);
            System.out.println("ExamSubmission: " + questions.size() + " soru bulundu");

            // Otomatik puanlama
            double totalScore = 0.0;
            double maxScore = 0.0;
            Map<String, Boolean> correctness = new HashMap<>();

            for (Question question : questions) {
                maxScore += (question.getPoints() != null ? question.getPoints() : 1.0);

                String studentAnswer = request.answers.get(String.valueOf(question.getId()));
                boolean isCorrect = false;

                if (question.getType() == Question.QuestionType.MULTIPLE_CHOICE) {
                    // Çoktan seçmeli - doğru cevap index kontrolü
                    if (studentAnswer != null && question.getCorrectOptionIndex() != null) {
                        try {
                            int answerIndex = Integer.parseInt(studentAnswer);
                            isCorrect = (answerIndex == question.getCorrectOptionIndex());
                        } catch (NumberFormatException e) {
                            isCorrect = false;
                        }
                    }
                } else {
                    // Klasik soru - anahtar kelime kontrolü (basit)
                    if (studentAnswer != null && question.getAnswerKey() != null) {
                        String answerLower = studentAnswer.toLowerCase().trim();
                        String keyLower = question.getAnswerKey().toLowerCase().trim();
                        isCorrect = answerLower.contains(keyLower) || keyLower.contains(answerLower);
                    }
                }

                if (isCorrect) {
                    totalScore += (question.getPoints() != null ? question.getPoints() : 1.0);
                }
                correctness.put(String.valueOf(question.getId()), isCorrect);

                System.out.println("ExamSubmission: Soru " + question.getId() + " - Cevap: " + studentAnswer + ", Doğru: " + isCorrect);
            }

            // Submission oluştur
            ExamSubmission submission = new ExamSubmission();
            submission.setExam(exam);
            submission.setStudent(student);
            submission.setAnswers(objectMapper.writeValueAsString(request.answers));
            submission.setScore(totalScore);
            submission.setMaxScore(maxScore);
            submission.setIsGraded(true);

            ExamSubmission saved = submissionRepository.save(submission);
            System.out.println("ExamSubmission: Kaydedildi - Score: " + totalScore + "/" + maxScore);

            // Response
            Map<String, Object> response = new HashMap<>();
            response.put("submission", saved);
            response.put("correctness", correctness);
            response.put("score", totalScore);
            response.put("maxScore", maxScore);
            response.put("percentage", maxScore > 0 ? (totalScore / maxScore * 100) : 0);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("ExamSubmission: Hata - " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", "Sınav gönderilemedi: " + e.getMessage()));
        }
    }

    // Öğrencinin tüm gönderimlerini getir
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<SubmissionDTO>> getStudentSubmissions(@PathVariable Long studentId) {
        try {
            List<ExamSubmission> submissions = submissionRepository.findByStudentId(studentId);
            System.out.println("ExamSubmission: Öğrenci " + studentId + " için " + submissions.size() + " gönderim bulundu");
            List<SubmissionDTO> dtos = submissions.stream().map(this::toDTO).collect(java.util.stream.Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            System.err.println("ExamSubmission: Hata - " + e.getMessage());
            return ResponseEntity.status(500).body(new java.util.ArrayList<>());
        }
    }

    // Sınavın tüm gönderimlerini getir (öğretmen için)
    @GetMapping("/exam/{examId}")
    public ResponseEntity<List<SubmissionDTO>> getExamSubmissions(@PathVariable Long examId) {
        try {
            List<ExamSubmission> submissions = submissionRepository.findByExamId(examId);
            System.out.println("ExamSubmission: Sınav " + examId + " için " + submissions.size() + " gönderim bulundu");
            List<SubmissionDTO> dtos = submissions.stream().map(this::toDTO).collect(java.util.stream.Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            System.err.println("ExamSubmission: Hata - " + e.getMessage());
            return ResponseEntity.status(500).body(new java.util.ArrayList<>());
        }
    }

    // Belirli bir öğrencinin belirli sınavdaki gönderimini getir
    @GetMapping("/exam/{examId}/student/{studentId}")
    public ResponseEntity<?> getSubmission(@PathVariable Long examId, @PathVariable Long studentId) {
        try {
            Optional<ExamSubmission> submission = submissionRepository.findByExamIdAndStudentId(examId, studentId);
            if (submission.isEmpty()) {
                return ResponseEntity.ok(Map.of("submitted", false));
            }
            return ResponseEntity.ok(submission.get());
        } catch (Exception e) {
            System.err.println("ExamSubmission: Hata - " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}

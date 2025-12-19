package com.example.online_egitim_sinav_kod.controller;

import com.example.online_egitim_sinav_kod.model.Exam;
import com.example.online_egitim_sinav_kod.model.Question;
import com.example.online_egitim_sinav_kod.model.Submission;
import com.example.online_egitim_sinav_kod.model.User;
import com.example.online_egitim_sinav_kod.repository.ExamRepository;
import com.example.online_egitim_sinav_kod.repository.QuestionRepository;
import com.example.online_egitim_sinav_kod.repository.SubmissionRepository;
import com.example.online_egitim_sinav_kod.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/submissions")
public class SubmissionController {

    private final ExamRepository examRepository;
    private final QuestionRepository questionRepository;
    private final SubmissionRepository submissionRepository;
    private final UserRepository userRepository;

    public SubmissionController(ExamRepository examRepository, QuestionRepository questionRepository, SubmissionRepository submissionRepository, UserRepository userRepository) {
        this.examRepository = examRepository;
        this.questionRepository = questionRepository;
        this.submissionRepository = submissionRepository;
        this.userRepository = userRepository;
    }

    public static class SubmissionRequest {
        public String answerText; // for open-ended
        public String selectedOptions; // e.g. "0||2||1"
        public Long studentId; // optional, if not provided use principal (not included here for simplicity)
    }

    @PostMapping("/exam/{examId}")
    public ResponseEntity<Submission> submitExam(@PathVariable Long examId, @RequestBody SubmissionRequest req) {
        Optional<Exam> oe = examRepository.findById(examId);
        if (oe.isEmpty()) return ResponseEntity.notFound().build();
        Exam exam = oe.get();
        Submission s = new Submission();
        s.setExam(exam);
        s.setAnswerText(req.answerText);
        s.setSelectedOptions(req.selectedOptions);
        s.setSubmittedAt(LocalDateTime.now());

        // set student if provided
        if (req.studentId != null) {
            userRepository.findById(req.studentId).ifPresent(s::setStudent);
        }

        // auto-grade both test and klasik (anahtar karşılaştırma)
        List<Question> questions = questionRepository.findByExamIdOrderByIdAsc(examId);
        double earned = 0.0;

        List<String> pickedOptions = req.selectedOptions != null
                ? Arrays.stream(req.selectedOptions.split("\\|\\|")).map(String::trim).collect(Collectors.toList())
                : java.util.Collections.emptyList();

        List<String> writtenAnswers = req.answerText != null
                ? Arrays.stream(req.answerText.split("\\|\\|")).map(String::trim).collect(Collectors.toList())
                : java.util.Collections.emptyList();

        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            double points = q.getPoints() != null ? q.getPoints() : 1.0;

            if (q.getType() == Question.QuestionType.MULTIPLE_CHOICE && q.getCorrectOptionIndex() != null) {
                if (i < pickedOptions.size()) {
                    try {
                        int pick = Integer.parseInt(pickedOptions.get(i));
                        if (pick == q.getCorrectOptionIndex()) {
                            earned += points;
                        }
                    } catch (NumberFormatException ignored) {}
                }
            } else if (q.getType() == Question.QuestionType.CLASSIC && q.getAnswerKey() != null) {
                if (i < writtenAnswers.size()) {
                    String ans = writtenAnswers.get(i);
                    if (!ans.isEmpty() && ans.equalsIgnoreCase(q.getAnswerKey().trim())) {
                        earned += points;
                    }
                }
            }
        }

        s.setScore((int) Math.round(earned));

        Submission saved = submissionRepository.save(s);
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/{id}/grade")
    public ResponseEntity<Submission> gradeSubmission(@PathVariable Long id, @RequestParam double score) {
        return submissionRepository.findById(id).map(s -> {
            s.setScore((int) Math.round(score)); // double'ı Integer'a çevir
            submissionRepository.save(s);
            return ResponseEntity.ok(s);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<Submission>> listByStudent(@PathVariable Long studentId) {
        List<Submission> list = submissionRepository.findAll().stream()
                .filter(s -> s.getStudent() != null && s.getStudent().getId().equals(studentId))
                .toList();
        return ResponseEntity.ok(list);
    }
}

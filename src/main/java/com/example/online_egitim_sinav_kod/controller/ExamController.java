package com.example.online_egitim_sinav_kod.controller;

import com.example.online_egitim_sinav_kod.model.Exam;
import com.example.online_egitim_sinav_kod.model.Question;
import com.example.online_egitim_sinav_kod.model.Question.QuestionType;
import com.example.online_egitim_sinav_kod.model.Role;
import com.example.online_egitim_sinav_kod.model.User;
import com.example.online_egitim_sinav_kod.repository.CourseRepository;
import com.example.online_egitim_sinav_kod.repository.ExamRepository;
import com.example.online_egitim_sinav_kod.repository.QuestionRepository;
import com.example.online_egitim_sinav_kod.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.time.LocalDateTime;
import java.time.Instant;

@RestController
@RequestMapping("/api/exams")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"}, allowedHeaders = "*")
public class ExamController {

    private final ExamRepository examRepository;
    private final CourseRepository courseRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;

    public ExamController(ExamRepository examRepository, CourseRepository courseRepository, QuestionRepository questionRepository, UserRepository userRepository) {
        this.examRepository = examRepository;
        this.courseRepository = courseRepository;
        this.questionRepository = questionRepository;
        this.userRepository = userRepository;
    }

    // DTO sınıfları
    public static class ExamDTO {
        public Long id;
        public String title;
        public String description;
        public SimpleCourseDTO course;
        public SimpleUserDTO createdBy;
        public LocalDateTime startTime;
        public LocalDateTime endTime;
        public Integer duration;
        public Boolean isActive;
        public Boolean isTest;
        public Instant createdAt;
        public int questionCount;
    }

    public static class SimpleCourseDTO {
        public Long id;
        public String title;
        public String description;
        public SimpleUserDTO teacher;
    }

    public static class SimpleUserDTO {
        public Long id;
        public String username;
        public String fullName;
        public Role role;  // String yerine Role enum
    }

    public static class ExamRequest {
        public String title;
        public String description;
        public Long courseId;
        public boolean isTest;
        public String startTime;
        public String endTime;
        public Long createdById;
    }

    public static class QuestionRequest {
        public String text;
        public String[] options;
        public Integer correctOptionIndex;
        public Double points = 1.0;
        public String type = "MULTIPLE_CHOICE";
        public String answerKey;
    }

    // Her 5 dakikada bir sınav aktiflik durumunu kontrol et
    @Scheduled(fixedRate = 300000) // 5 dakika = 300000 ms
    public void updateExamActiveStatus() {
        try {
            LocalDateTime now = LocalDateTime.now();
            List<Exam> allExams = examRepository.findAll();

            for (Exam exam : allExams) {
                boolean shouldBeActive = true;

                // Başlangıç zamanı henüz gelmemişse pasif
                if (exam.getStartTime() != null && exam.getStartTime().isAfter(now)) {
                    shouldBeActive = false;
                }

                // Bitiş zamanı geçmişse pasif
                if (exam.getEndTime() != null && exam.getEndTime().isBefore(now)) {
                    shouldBeActive = false;
                }

                // Durumu değiştir
                if (exam.getIsActive() == null || exam.getIsActive() != shouldBeActive) {
                    exam.setIsActive(shouldBeActive);
                    examRepository.save(exam);
                    System.out.println("ExamController: Sınav " + exam.getId() + " aktiflik durumu güncellendi: " + shouldBeActive);
                }
            }
        } catch (Exception e) {
            System.err.println("ExamController: Aktiflik kontrolü hatası: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createExam(@RequestBody ExamRequest req) {
        try {
            System.out.println("ExamController: Sınav oluşturuluyor - Title: " + req.title + ", CourseId: " + req.courseId + ", CreatedById: " + req.createdById);

            Exam e = new Exam();
            e.setTitle(req.title);
            e.setDescription(req.description);
            e.setTest(req.isTest);
            
            LocalDateTime startTime = null;
            LocalDateTime endTime = null;
            LocalDateTime now = LocalDateTime.now();

            if (StringUtils.hasText(req.startTime)) {
                startTime = LocalDateTime.parse(req.startTime);
                e.setStartTime(startTime);
            }
            if (StringUtils.hasText(req.endTime)) {
                endTime = LocalDateTime.parse(req.endTime);
                e.setEndTime(endTime);
            }
            
            // Süreyi otomatik hesapla (dakika cinsinden)
            if (startTime != null && endTime != null) {
                long durationMinutes = java.time.Duration.between(startTime, endTime).toMinutes();
                e.setDuration((int) durationMinutes);
            }
            
            // Aktiflik durumunu belirle
            boolean isActive = true;
            if (startTime != null && startTime.isAfter(now)) {
                isActive = false; // Henüz başlamamış
            }
            if (endTime != null && endTime.isBefore(now)) {
                isActive = false; // Süresi dolmuş
            }
            e.setIsActive(isActive);

            // Course kontrolü
            if (req.courseId != null) {
                var courseOpt = courseRepository.findById(req.courseId);
                if (courseOpt.isPresent()) {
                    e.setCourse(courseOpt.get());
                    System.out.println("ExamController: Course bulundu ve atandı: " + courseOpt.get().getTitle());
                } else {
                    System.err.println("ExamController: Course bulunamadı ID: " + req.courseId);
                    return ResponseEntity.badRequest().body(Map.of("error", "Ders bulunamadı"));
                }
            } else {
                System.err.println("ExamController: CourseId null!");
                return ResponseEntity.badRequest().body(Map.of("error", "Ders ID gerekli"));
            }
            
            // User kontrolü
            if (req.createdById != null) {
                var userOpt = userRepository.findById(req.createdById);
                if (userOpt.isPresent()) {
                    e.setCreatedBy(userOpt.get());
                    System.out.println("ExamController: Kullanıcı bulundu ve atandı: " + userOpt.get().getUsername());
                } else {
                    System.err.println("ExamController: Kullanıcı bulunamadı ID: " + req.createdById);
                }
            }
            
            Exam saved = examRepository.save(e);
            System.out.println("ExamController: Sınav kaydedildi - ID: " + saved.getId() +
                             ", Course ID: " + (saved.getCourse() != null ? saved.getCourse().getId() : "null") +
                             ", Active: " + saved.getIsActive() +
                             ", StartTime: " + saved.getStartTime() +
                             ", EndTime: " + saved.getEndTime());

            // Veritabanında gerçekten kaydedildiğini doğrula
            var verifyExam = examRepository.findById(saved.getId());
            if (verifyExam.isPresent()) {
                System.out.println("ExamController: Sınav doğrulandı - DB'de mevcut");

                // Hemen course'a göre sınavları kontrol et
                List<Exam> courseExams = examRepository.findByCourseId(saved.getCourse().getId());
                System.out.println("ExamController: Course " + saved.getCourse().getId() + " için toplam " + courseExams.size() + " sınav var");
            } else {
                System.err.println("ExamController: HATA - Sınav kaydedildi ama doğrulanamadı!");
            }

            return ResponseEntity.ok(saved);
        } catch (Exception ex) {
            System.err.println("ExamController: Sınav oluşturma hatası: " + ex.getMessage());
            ex.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", "Sınav oluşturulamadı: " + ex.getMessage()));
        }
    }

    @PostMapping("/{examId}/questions")
    public ResponseEntity<?> addQuestion(@PathVariable Long examId, @RequestBody QuestionRequest req) {
        try {
            Optional<Exam> oe = examRepository.findById(examId);
            if (oe.isEmpty()) return ResponseEntity.notFound().build();

            Question q = new Question();
            q.setExam(oe.get());
            q.setText(req.text);
            q.setPoints(req.points);
            q.setType(QuestionType.valueOf(req.type != null ? req.type : "MULTIPLE_CHOICE"));
            q.setAnswerKey(req.answerKey);

            // Çoktan seçmeli soru ise seçenekleri kaydet
            if (req.options != null && req.options.length > 0) {
                q.setOptions(String.join("||", req.options));
                q.setCorrectOptionIndex(req.correctOptionIndex);
            }

            Question saved = questionRepository.save(q);
            System.out.println("ExamController: Soru eklendi, ID: " + saved.getId() + ", Exam ID: " + examId);
            return ResponseEntity.ok(saved);
        } catch (Exception ex) {
            System.err.println("ExamController: Soru ekleme hatası: " + ex.getMessage());
            ex.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @GetMapping("/{examId}")
    public ResponseEntity<Exam> getExam(@PathVariable Long examId) {
        Optional<Exam> exam = examRepository.findById(examId);
        if (exam.isEmpty()) return ResponseEntity.notFound().build();

        // Questions'ları da yükle
        exam.get().getQuestions().size();
        return ResponseEntity.ok(exam.get());
    }

    @GetMapping("/{examId}/questions")
    public ResponseEntity<List<Question>> getQuestions(@PathVariable Long examId) {
        List<Question> questions = questionRepository.findByExamIdOrderByIdAsc(examId);
        return ResponseEntity.ok(questions);
    }

    @GetMapping
    public ResponseEntity<List<Exam>> listAllExams() {
        try {
            List<Exam> allExams = examRepository.findAll();
            System.out.println("ExamController: Total exams in database: " + allExams.size());
            for (Exam exam : allExams) {
                System.out.println("ExamController: Exam ID: " + exam.getId() + ", Title: " + exam.getTitle() + ", Course: " + (exam.getCourse() != null ? exam.getCourse().getId() : "null"));
            }
            return ResponseEntity.ok(allExams);
        } catch (Exception e) {
            System.err.println("ExamController: Error listing all exams: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(new java.util.ArrayList<>());
        }
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<ExamDTO>> listByCourse(@PathVariable Long courseId) {
        try {
            System.out.println("ExamController: Ders için sınavlar yükleniyor - CourseId: " + courseId);

            if (!courseRepository.existsById(courseId)) {
                System.out.println("ExamController: Ders bulunamadı ID: " + courseId);
                return ResponseEntity.ok(new java.util.ArrayList<>());
            }

            List<Exam> list = examRepository.findByCourseId(courseId);
            System.out.println("ExamController: " + list.size() + " sınav bulundu - CourseId: " + courseId);

            LocalDateTime now = LocalDateTime.now();
            List<ExamDTO> dtoList = new java.util.ArrayList<>();

            for (Exam exam : list) {
                try {
                    ExamDTO dto = new ExamDTO();
                    dto.id = exam.getId();
                    dto.title = exam.getTitle();
                    dto.description = exam.getDescription();
                    dto.startTime = exam.getStartTime();
                    dto.endTime = exam.getEndTime();
                    dto.duration = exam.getDuration();
                    dto.createdAt = exam.getCreatedAt();
                    dto.isTest = exam.getIsTest();

                    // Course bilgisi
                    if (exam.getCourse() != null) {
                        SimpleCourseDTO courseDTO = new SimpleCourseDTO();
                        courseDTO.id = exam.getCourse().getId();
                        courseDTO.title = exam.getCourse().getTitle();
                        courseDTO.description = exam.getCourse().getDescription();

                        if (exam.getCourse().getTeacher() != null) {
                            SimpleUserDTO teacherDTO = new SimpleUserDTO();
                            teacherDTO.id = exam.getCourse().getTeacher().getId();
                            teacherDTO.username = exam.getCourse().getTeacher().getUsername();
                            teacherDTO.fullName = exam.getCourse().getTeacher().getFullName();
                            teacherDTO.role = exam.getCourse().getTeacher().getRole();
                            courseDTO.teacher = teacherDTO;
                        }
                        dto.course = courseDTO;
                    }

                    // CreatedBy bilgisi
                    if (exam.getCreatedBy() != null) {
                        SimpleUserDTO userDTO = new SimpleUserDTO();
                        userDTO.id = exam.getCreatedBy().getId();
                        userDTO.username = exam.getCreatedBy().getUsername();
                        userDTO.fullName = exam.getCreatedBy().getFullName();
                        userDTO.role = exam.getCreatedBy().getRole();
                        dto.createdBy = userDTO;
                    }

                    // Aktiflik kontrolü
                    boolean shouldBeActive = true;
                    if (exam.getStartTime() != null && exam.getStartTime().isAfter(now)) {
                        shouldBeActive = false;
                    }
                    if (exam.getEndTime() != null && exam.getEndTime().isBefore(now)) {
                        shouldBeActive = false;
                    }

                    if (exam.getIsActive() == null || exam.getIsActive() != shouldBeActive) {
                        exam.setIsActive(shouldBeActive);
                        examRepository.save(exam);
                        System.out.println("ExamController: Sınav " + exam.getId() + " aktiflik durumu güncellendi: " + shouldBeActive);
                    }

                    dto.isActive = shouldBeActive;

                    // Soru sayısı
                    try {
                        dto.questionCount = questionRepository.findByExamIdOrderByIdAsc(exam.getId()).size();
                    } catch (Exception qe) {
                        System.err.println("ExamController: Soru sayısı alınırken hata: " + qe.getMessage());
                        dto.questionCount = 0;
                    }

                    dtoList.add(dto);

                    System.out.println("ExamController: Sınav " + dto.id + " (" + dto.title +
                                     ") - Active: " + dto.isActive +
                                     ", Questions: " + dto.questionCount);
                } catch (Exception e) {
                    System.err.println("ExamController: Sınav " + exam.getId() + " işlenirken hata: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            System.out.println("ExamController: " + dtoList.size() + " sınav DTO döndürülüyor");
            return ResponseEntity.ok(dtoList);
        } catch (Exception e) {
            System.err.println("ExamController: listByCourse hatası: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(new java.util.ArrayList<>());
        }
    }

    @DeleteMapping("/{examId}/questions/{questionId}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long examId, @PathVariable Long questionId) {
        try {
            questionRepository.deleteById(questionId);
            return ResponseEntity.ok().build();
        } catch (Exception ex) {
            return ResponseEntity.badRequest().build();
        }
    }
}

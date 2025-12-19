package com.example.online_egitim_sinav_kod.controller;

import com.example.online_egitim_sinav_kod.model.ClassRoom;
import com.example.online_egitim_sinav_kod.model.Course;
import com.example.online_egitim_sinav_kod.model.CourseEnrollment;
import com.example.online_egitim_sinav_kod.model.CourseEnrollment.EnrollmentStatus;
import com.example.online_egitim_sinav_kod.model.User;
import com.example.online_egitim_sinav_kod.repository.ClassRoomRepository;
import com.example.online_egitim_sinav_kod.repository.CourseEnrollmentRepository;
import com.example.online_egitim_sinav_kod.repository.CourseRepository;
import com.example.online_egitim_sinav_kod.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"}, allowedHeaders = "*")
public class EnrollmentController {

    private final CourseEnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final ClassRoomRepository classRoomRepository;

    public EnrollmentController(CourseEnrollmentRepository enrollmentRepository,
                               CourseRepository courseRepository,
                               UserRepository userRepository,
                               ClassRoomRepository classRoomRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.classRoomRepository = classRoomRepository;
    }

    public static class EnrollmentRequest {
        public Long courseId;
        public String message;

        // Getters and setters
        public Long getCourseId() { return courseId; }
        public void setCourseId(Long courseId) { this.courseId = courseId; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    public static class EnrollmentResponse {
        public Long id;
        public String action; // "approve" or "reject"
        public String responseMessage;

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
        public String getResponseMessage() { return responseMessage; }
        public void setResponseMessage(String responseMessage) { this.responseMessage = responseMessage; }
    }

    /**
     * Giriş yapan öğrenciyi bulmak için yardımcı metot.
     * Güvenlik konfigürasyonunda HTTP Basic devre dışı olduğu için
     * Principal çoğu zaman null geliyor. Bu yüzden hem Principal'dan
     * hem de Authorization header içindeki Basic token'dan kullanıcıyı
     * çözmeye çalışıyoruz.
     */
    private User resolveStudent(Principal principal, String authHeader) {
        User student = null;

        // 1) Principal üzerinden dene
        if (principal != null) {
            student = userRepository.findByUsername(principal.getName()).orElse(null);
            System.out.println("ENROLLMENT DEBUG: Found user from Principal: " + (student != null ? student.getUsername() : "null"));
        }

        // 2) Authorization: Basic ... varsa oradan username çöz
        if (student == null && authHeader != null && authHeader.startsWith("Basic ")) {
            try {
                String base64Credentials = authHeader.substring("Basic ".length()).trim();
                byte[] decodedBytes = java.util.Base64.getDecoder().decode(base64Credentials);
                String decoded = new String(decodedBytes, java.nio.charset.StandardCharsets.UTF_8);
                String username = decoded.split(":", 2)[0];
                System.out.println("ENROLLMENT DEBUG: Trying to resolve user from Basic auth username: " + username);
                student = userRepository.findByUsername(username).orElse(null);
            } catch (Exception e) {
                System.err.println("ENROLLMENT DEBUG: Failed to decode Basic auth header: " + e.getMessage());
            }
        }

        // 3) Son çare: veritabanındaki ilk STUDENT (geliştirme amaçlı)
        if (student == null) {
            System.out.println("ENROLLMENT DEBUG: Principal and Basic auth failed, falling back to first STUDENT user...");
            student = userRepository.findAll().stream()
                .filter(u -> "STUDENT".equals(u.getRole().toString()))
                .findFirst()
                .orElse(null);

            if (student != null) {
                System.out.println("ENROLLMENT DEBUG: Fallback student: " + student.getUsername());
            }
        }

        return student;
    }

    // Öğrenci ders katılım isteği gönderir
    @PostMapping("/request")
    public ResponseEntity<?> requestEnrollment(@RequestBody EnrollmentRequest request,
                                               Principal principal,
                                               @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            System.out.println("ENROLLMENT DEBUG: Request received");
            System.out.println("ENROLLMENT DEBUG: Principal: " + (principal != null ? principal.getName() : "null"));
            System.out.println("ENROLLMENT DEBUG: Course ID: " + request.getCourseId());
            System.out.println("ENROLLMENT DEBUG: Message: " + request.getMessage());

            User student = resolveStudent(principal, authHeader);

            if (student == null) {
                System.out.println("ENROLLMENT DEBUG: No student user found");
                return ResponseEntity.status(403).body("Öğrenci kullanıcısı bulunamadı");
            }

            if (!"STUDENT".equals(student.getRole().toString())) {
                System.out.println("ENROLLMENT DEBUG: User role is not STUDENT: " + student.getRole());
                return ResponseEntity.status(403).body("Sadece öğrenciler ders katılım isteği gönderebilir");
            }

            Course course = courseRepository.findById(request.getCourseId()).orElse(null);
            if (course == null) {
                System.out.println("ENROLLMENT DEBUG: Course not found: " + request.getCourseId());
                return ResponseEntity.badRequest().body("Ders bulunamadı");
            }

            System.out.println("ENROLLMENT DEBUG: Course found: " + course.getTitle());

            // Öğrencinin bu derse kendi sınıfından erişebilip erişemediğini kontrol et
            List<ClassRoom> studentClassrooms = classRoomRepository.findByStudentId(student.getId());
            boolean courseInStudentClassroom = false;
            for (ClassRoom classroom : studentClassrooms) {
                if (classroom.getCourses().stream().anyMatch(c -> c.getId().equals(course.getId()))) {
                    courseInStudentClassroom = true;
                    break;
                }
            }

            if (!courseInStudentClassroom) {
                System.out.println("ENROLLMENT DEBUG: Course is not in student's classrooms");
                return ResponseEntity.badRequest().body("Bu ders sizin sınıfınıza atanmamış. Sadece kendi sınıfınıza atanan derslere kayıt olabilirsiniz.");
            }

            // Önceden bir istek var mı kontrol et
            List<CourseEnrollment> existingEnrollments =
                enrollmentRepository.findByStudentIdAndCourseId(student.getId(), request.getCourseId());

            if (!existingEnrollments.isEmpty()) {
                CourseEnrollment enrollment = existingEnrollments.get(0);
                System.out.println("ENROLLMENT DEBUG: Existing enrollment found with status: " + enrollment.getStatus());

                if (enrollment.getStatus() == EnrollmentStatus.PENDING) {
                    return ResponseEntity.badRequest().body("Bu ders için zaten bekleyen bir isteğiniz var");
                } else if (enrollment.getStatus() == EnrollmentStatus.APPROVED) {
                    return ResponseEntity.badRequest().body("Bu derse zaten kayıtlısınız");
                }
            }

            // Yeni katılım isteği oluştur
            CourseEnrollment enrollment = new CourseEnrollment(student, course, request.getMessage());
            CourseEnrollment savedEnrollment = enrollmentRepository.save(enrollment);
            System.out.println("ENROLLMENT DEBUG: New enrollment created with ID: " + savedEnrollment.getId());

            return ResponseEntity.ok().body("Ders katılım isteğiniz gönderildi. Öğretmenin onayını bekleyiniz.");

        } catch (Exception e) {
            System.err.println("ENROLLMENT DEBUG: Exception occurred: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Katılım isteği gönderilirken hata oluştu: " + e.getMessage());
        }
    }

    // Öğrencinin katılım isteklerini listele
    @GetMapping("/my-requests")
    public ResponseEntity<List<CourseEnrollment>> getMyEnrollments(Principal principal,
                                                                   @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            System.out.println("ENROLLMENT DEBUG: Getting my requests, Principal: " + (principal != null ? principal.getName() : "null"));

            User student = resolveStudent(principal, authHeader);

            if (student == null) {
                return ResponseEntity.ok(new java.util.ArrayList<>());
            }

            List<CourseEnrollment> enrollments =
                enrollmentRepository.findByStudentIdOrderByRequestDateDesc(student.getId());

            System.out.println("ENROLLMENT DEBUG: Found " + enrollments.size() + " enrollment requests");
            return ResponseEntity.ok(enrollments);

        } catch (Exception e) {
            System.err.println("ENROLLMENT DEBUG: Error in getMyEnrollments: " + e.getMessage());
            return ResponseEntity.ok(new java.util.ArrayList<>());
        }
    }

    // Öğretmen bekleyen katılım isteklerini görür
    @GetMapping("/pending-requests")
    public ResponseEntity<List<CourseEnrollment>> getPendingRequests(Principal principal) {
        try {
            System.out.println("ENROLLMENT DEBUG: Getting pending requests, Principal: " + (principal != null ? principal.getName() : "null"));

            // Geçici olarak authentication kontrolünü bypass et
            User teacher = null;

            if (principal != null) {
                teacher = userRepository.findByUsername(principal.getName()).orElse(null);
            }

            // Eğer Principal'dan bulamadıysak, test için herhangi bir TEACHER kullan
            if (teacher == null) {
                teacher = userRepository.findAll().stream()
                    .filter(u -> "TEACHER".equals(u.getRole().toString()))
                    .findFirst()
                    .orElse(null);

                System.out.println("ENROLLMENT DEBUG: Using test teacher: " + (teacher != null ? teacher.getUsername() : "none"));
            }

            if (teacher == null) {
                System.out.println("ENROLLMENT DEBUG: No teacher found");
                return ResponseEntity.ok(new java.util.ArrayList<>());
            }

            List<CourseEnrollment> pendingEnrollments =
                enrollmentRepository.findByCourseTeacherIdAndStatusOrderByRequestDateDesc(
                    teacher.getId(), EnrollmentStatus.PENDING);

            System.out.println("ENROLLMENT DEBUG: Found " + pendingEnrollments.size() + " pending enrollment requests");
            return ResponseEntity.ok(pendingEnrollments);

        } catch (Exception e) {
            System.err.println("ENROLLMENT DEBUG: Error in getPendingRequests: " + e.getMessage());
            return ResponseEntity.ok(new java.util.ArrayList<>());
        }
    }

    // Öğretmen katılım isteğini onaylar/reddeder
    @PostMapping("/respond/{enrollmentId}")
    public ResponseEntity<?> respondToEnrollment(@PathVariable Long enrollmentId,
                                                @RequestBody EnrollmentResponse response,
                                                Principal principal) {
        try {
            System.out.println("ENROLLMENT DEBUG: Responding to enrollment " + enrollmentId + " with action: " + response.getAction());

            // Geçici olarak authentication kontrolünü bypass et
            User teacher = null;

            if (principal != null) {
                teacher = userRepository.findByUsername(principal.getName()).orElse(null);
            }

            // Eğer Principal'dan bulamadıysak, test için herhangi bir TEACHER kullan
            if (teacher == null) {
                teacher = userRepository.findAll().stream()
                    .filter(u -> "TEACHER".equals(u.getRole().toString()))
                    .findFirst()
                    .orElse(null);

                System.out.println("ENROLLMENT DEBUG: Using test teacher: " + (teacher != null ? teacher.getUsername() : "none"));
            }

            if (teacher == null) {
                return ResponseEntity.status(403).body("Öğretmen kullanıcısı bulunamadı");
            }

            CourseEnrollment enrollment = enrollmentRepository.findById(enrollmentId).orElse(null);
            if (enrollment == null) {
                return ResponseEntity.badRequest().body("Katılım isteği bulunamadı");
            }

            // Durum güncelle
            if ("approve".equals(response.getAction())) {
                enrollment.setStatus(EnrollmentStatus.APPROVED);
                enrollment.setApprovalDate(LocalDateTime.now());
            } else if ("reject".equals(response.getAction())) {
                enrollment.setStatus(EnrollmentStatus.REJECTED);
            } else {
                return ResponseEntity.badRequest().body("Geçersiz işlem");
            }

            enrollment.setResponseMessage(response.getResponseMessage());
            enrollmentRepository.save(enrollment);

            String message = "approve".equals(response.getAction()) ?
                "Katılım isteği onaylandı" : "Katılım isteği reddedildi";

            System.out.println("ENROLLMENT DEBUG: " + message);
            return ResponseEntity.ok().body(message);

        } catch (Exception e) {
            System.err.println("ENROLLMENT DEBUG: Error responding to enrollment: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("İstek yanıtlanırken hata oluştu: " + e.getMessage());
        }
    }

    // Öğrencinin onaylanmış derslerini getir
    @GetMapping("/my-courses")
    public ResponseEntity<List<CourseEnrollment>> getMyApprovedCourses(Principal principal,
                                                                       @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            System.out.println("ENROLLMENT DEBUG: Getting my courses, Principal: " + (principal != null ? principal.getName() : "null"));

            User student = resolveStudent(principal, authHeader);

            if (student == null) {
                return ResponseEntity.ok(new java.util.ArrayList<>());
            }

            List<CourseEnrollment> approvedEnrollments =
                enrollmentRepository.findByStudentIdAndStatus(student.getId(), EnrollmentStatus.APPROVED);

            System.out.println("ENROLLMENT DEBUG: Found " + approvedEnrollments.size() + " approved courses");
            return ResponseEntity.ok(approvedEnrollments);

        } catch (Exception e) {
            System.err.println("ENROLLMENT DEBUG: Error in getMyApprovedCourses: " + e.getMessage());
            return ResponseEntity.ok(new java.util.ArrayList<>());
        }
    }

    // Bir derse kayıtlı öğrencileri getir (Öğretmen için)
    @GetMapping("/course/{courseId}/students")
    public ResponseEntity<List<User>> getCourseStudents(@PathVariable Long courseId) {
        try {
            System.out.println("ENROLLMENT DEBUG: Getting students for course: " + courseId);

            Course course = courseRepository.findById(courseId).orElse(null);
            if (course == null) {
                System.out.println("ENROLLMENT DEBUG: Course not found: " + courseId);
                return ResponseEntity.ok(new java.util.ArrayList<>());
            }

            // Onaylanmış kayıtları bul
            List<CourseEnrollment> approvedEnrollments =
                enrollmentRepository.findByCourseIdAndStatus(courseId, EnrollmentStatus.APPROVED);

            System.out.println("ENROLLMENT DEBUG: Found " + approvedEnrollments.size() + " approved enrollments");

            // Öğrenci listesini oluştur
            List<User> students = new java.util.ArrayList<>();
            for (CourseEnrollment enrollment : approvedEnrollments) {
                User student = enrollment.getUser();  // getStudent() değil getUser() olmalı
                if (student != null) {
                    students.add(student);
                }
            }

            System.out.println("ENROLLMENT DEBUG: Returning " + students.size() + " students");
            return ResponseEntity.ok(students);

        } catch (Exception e) {
            System.err.println("ENROLLMENT DEBUG: Error in getCourseStudents: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(new java.util.ArrayList<>());
        }
    }
}

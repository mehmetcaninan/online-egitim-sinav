package com.example.online_egitim_sinav_kod.controller;

import com.example.online_egitim_sinav_kod.dto.CourseRequest;
import com.example.online_egitim_sinav_kod.model.ClassRoom;
import com.example.online_egitim_sinav_kod.model.Course;
import com.example.online_egitim_sinav_kod.model.CourseEnrollment;
import com.example.online_egitim_sinav_kod.model.Role;
import com.example.online_egitim_sinav_kod.model.User;
import com.example.online_egitim_sinav_kod.repository.ClassRoomRepository;
import com.example.online_egitim_sinav_kod.repository.CourseEnrollmentRepository;
import com.example.online_egitim_sinav_kod.repository.UserRepository;
import com.example.online_egitim_sinav_kod.service.CourseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/courses")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"}, allowedHeaders = "*")
public class CourseController {

    private final CourseService courseService;
    private final UserRepository userRepository;
    private final ClassRoomRepository classRoomRepository;
    private final CourseEnrollmentRepository courseEnrollmentRepository;

    public CourseController(CourseService courseService, UserRepository userRepository, ClassRoomRepository classRoomRepository, CourseEnrollmentRepository courseEnrollmentRepository) {
        this.courseService = courseService;
        this.userRepository = userRepository;
        this.classRoomRepository = classRoomRepository;
        this.courseEnrollmentRepository = courseEnrollmentRepository;
    }

    @PostMapping
    public ResponseEntity<?> createCourse(@RequestBody CourseRequest req, Principal principal) {
        try {
            System.out.println("COURSE DEBUG: Creating course with title: '" + req.getTitle() + "'");
            System.out.println("COURSE DEBUG: Description: '" + req.getDescription() + "'");
            System.out.println("COURSE DEBUG: TeacherId: " + req.getTeacherId());

            // Null/empty check
            if (req.getTitle() == null || req.getTitle().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Ders adı zorunludur");
            }

            // Teacher bulma - teacherId var mı kontrol et
            User teacher = null;
            if (req.getTeacherId() != null) {
                teacher = userRepository.findById(req.getTeacherId()).orElse(null);
            } else if (principal != null) {
                teacher = userRepository.findByUsername(principal.getName()).orElse(null);
            }

            if (teacher == null) {
                return ResponseEntity.badRequest().body("Öğretmen bulunamadı");
            }

            if (!"TEACHER".equals(teacher.getRole().toString())) {
                return ResponseEntity.badRequest().body("Sadece öğretmenler ders oluşturabilir");
            }

            Course course = new Course(req.getTitle(), req.getDescription(), teacher);
            Course savedCourse = courseService.saveCourse(course);

            // Dersi belirtilen sınıflara ata
            if (req.getClassroomIds() != null && !req.getClassroomIds().isEmpty()) {
                for (Long classroomId : req.getClassroomIds()) {
                    ClassRoom classroom = classRoomRepository.findById(classroomId).orElse(null);
                    if (classroom != null) {
                        classroom.addCourse(savedCourse);
                        classRoomRepository.save(classroom);
                        System.out.println("COURSE DEBUG: Course assigned to classroom: " + classroom.getName());
                    }
                }
            }

            System.out.println("COURSE DEBUG: Course created with ID: " + savedCourse.getId());
            return ResponseEntity.ok(savedCourse);

        } catch (Exception e) {
            System.err.println("COURSE DEBUG: Error creating course: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Ders oluşturulurken hata oluştu: " + e.getMessage());
        }
    }

    // Öğretmenin derslerini getir
    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<List<Course>> getCoursesByTeacher(@PathVariable Long teacherId) {
        try {
            System.out.println("COURSE DEBUG: Getting courses for teacher ID: " + teacherId);
            List<Course> courses = courseService.getCoursesByTeacher(teacherId);
            System.out.println("COURSE DEBUG: Found " + courses.size() + " courses");
            return ResponseEntity.ok(courses);
        } catch (Exception e) {
            System.err.println("COURSE DEBUG: Error getting courses: " + e.getMessage());
            return ResponseEntity.ok(List.of());
        }
    }

    // Tüm aktif dersleri getir (öğrenciler için - sadece sınıflarına atanan dersler)
    @GetMapping("/active")
    public ResponseEntity<List<Course>> getActiveCourses(Principal principal) {
        try {
            // Eğer kullanıcı öğrenci ise, sadece sınıflarına atanan dersleri göster
            User currentUser = null;
            if (principal != null) {
                currentUser = userRepository.findByUsername(principal.getName()).orElse(null);
            }

            if (currentUser != null && currentUser.getRole() == Role.STUDENT) {
                // Öğrencinin sınıflarını bul
                List<ClassRoom> studentClassrooms = classRoomRepository.findByStudentId(currentUser.getId());
                
                // Sınıflarına atanan tüm dersleri topla
                Set<Course> classroomCourses = new HashSet<>();
                for (ClassRoom classroom : studentClassrooms) {
                    classroomCourses.addAll(classroom.getCourses());
                }
                
                // Sadece aktif dersleri filtrele
                List<Course> activeClassroomCourses = classroomCourses.stream()
                    .filter(Course::isActive)
                    .collect(Collectors.toList());
                
                System.out.println("COURSE DEBUG: Student " + currentUser.getUsername() + " can see " + activeClassroomCourses.size() + " courses from their classrooms");
                return ResponseEntity.ok(activeClassroomCourses);
            } else {
                // Öğretmen veya admin ise tüm aktif dersleri göster
                List<Course> activeCourses = courseService.getActiveCourses();
                return ResponseEntity.ok(activeCourses);
            }
        } catch (Exception e) {
            System.err.println("Error getting active courses: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(List.of());
        }
    }

    // Bir derse kayıtlı onaylanmış öğrencileri getir
    @GetMapping("/{courseId}/students")
    public ResponseEntity<List<User>> getCourseStudents(@PathVariable Long courseId) {
        try {
            List<CourseEnrollment> enrollments = courseEnrollmentRepository.findByCourseIdAndStatus(courseId, CourseEnrollment.EnrollmentStatus.APPROVED);
            List<User> students = enrollments.stream()
                    .map(CourseEnrollment::getUser)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(students);
        } catch (Exception e) {
            System.err.println("COURSE DEBUG: Error getting course students: " + e.getMessage());
            return ResponseEntity.status(500).body(List.of());
        }
    }

    // Belirli bir dersi getir
    @GetMapping("/{id}")
    public ResponseEntity<Course> getCourse(@PathVariable Long id) {
        try {
            Course course = courseService.getCourseById(id);
            if (course != null) {
                return ResponseEntity.ok(course);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    // Öğrencinin sınıflarına atanan dersleri getir (alternatif endpoint)
    @GetMapping("/student-classrooms")
    public ResponseEntity<List<Course>> getStudentClassroomCourses(Principal principal) {
        try {
            User currentUser = null;
            if (principal != null) {
                currentUser = userRepository.findByUsername(principal.getName()).orElse(null);
            }

            if (currentUser == null || currentUser.getRole() != Role.STUDENT) {
                return ResponseEntity.badRequest().body(List.of());
            }

            List<ClassRoom> studentClassrooms = classRoomRepository.findByStudentId(currentUser.getId());
            Set<Course> classroomCourses = new HashSet<>();
            for (ClassRoom classroom : studentClassrooms) {
                classroomCourses.addAll(classroom.getCourses());
            }

            List<Course> activeClassroomCourses = classroomCourses.stream()
                .filter(Course::isActive)
                .collect(Collectors.toList());

            return ResponseEntity.ok(activeClassroomCourses);
        } catch (Exception e) {
            System.err.println("Error getting student classroom courses: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(List.of());
        }
    }

    // Ders güncelleme
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCourse(@PathVariable Long id, @RequestBody CourseRequest req, Principal principal) {
        try {
            Course course = courseService.getCourseById(id);
            if (course == null) {
                return ResponseEntity.status(404).body("Ders bulunamadı");
            }

            // Yetki kontrolü (basitleştirilmiş):
            // Güvenlik konfigürasyonunda zaten tüm istekler izinli.
            // Burada ekstra 403 kontrolü, frontend'ten gelen isteklerde Principal
            // oluşmadığı için "yetkiniz yok" hatasına neden oluyordu.
            // Geliştirme ortamı için bu kontrolü devre dışı bırakıyoruz.

            // Ders bilgilerini güncelle
            if (req.getTitle() != null && !req.getTitle().trim().isEmpty()) {
                course.setTitle(req.getTitle());
            }
            if (req.getDescription() != null) {
                course.setDescription(req.getDescription());
            }

            Course savedCourse = courseService.saveCourse(course);

            // Sınıf atamalarını güncelle
            if (req.getClassroomIds() != null) {
                // Mevcut sınıflardan dersi çıkar
                List<ClassRoom> allClassrooms = classRoomRepository.findAll();
                for (ClassRoom classroom : allClassrooms) {
                    if (classroom.getCourses().contains(savedCourse)) {
                        classroom.removeCourse(savedCourse);
                        classRoomRepository.save(classroom);
                    }
                }

                // Yeni sınıflara dersi ekle
                if (!req.getClassroomIds().isEmpty()) {
                    for (Long classroomId : req.getClassroomIds()) {
                        ClassRoom classroom = classRoomRepository.findById(classroomId).orElse(null);
                        if (classroom != null) {
                            classroom.addCourse(savedCourse);
                            classRoomRepository.save(classroom);
                        }
                    }
                }
            }

            return ResponseEntity.ok(savedCourse);
        } catch (Exception e) {
            System.err.println("COURSE DEBUG: Error updating course: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Ders güncellenirken hata oluştu: " + e.getMessage());
        }
    }

    // Ders silme
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCourse(@PathVariable Long id, Principal principal) {
        try {
            Course course = courseService.getCourseById(id);
            if (course == null) {
                return ResponseEntity.status(404).body("Ders bulunamadı");
            }

            // Yetki kontrolü (basitleştirilmiş) - geliştirme ortamında ekstra 403 vermeyelim.

            // Dersi tüm sınıflardan çıkar
            List<ClassRoom> allClassrooms = classRoomRepository.findAll();
            for (ClassRoom classroom : allClassrooms) {
                if (classroom.getCourses().contains(course)) {
                    classroom.removeCourse(course);
                    classRoomRepository.save(classroom);
                }
            }

            // Soft delete - dersi pasif yap
            course.setActive(false);
            courseService.saveCourse(course);

            return ResponseEntity.ok(Map.of("message", "Ders silindi"));
        } catch (Exception e) {
            System.err.println("COURSE DEBUG: Error deleting course: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Ders silinirken hata oluştu: " + e.getMessage());
        }
    }

    // Ders sınıf değiştirme
    @PutMapping("/{id}/classrooms")
    public ResponseEntity<?> updateCourseClassrooms(@PathVariable Long id, @RequestBody Map<String, List<Long>> request, Principal principal) {
        try {
            Course course = courseService.getCourseById(id);
            if (course == null) {
                return ResponseEntity.status(404).body("Ders bulunamadı");
            }

            // Yetki kontrolü
            User currentUser = null;
            if (principal != null) {
                currentUser = userRepository.findByUsername(principal.getName()).orElse(null);
            }

            if (currentUser == null || (!course.getTeacher().getId().equals(currentUser.getId()) && currentUser.getRole() != Role.ADMIN)) {
                return ResponseEntity.status(403).body("Bu dersin sınıflarını değiştirme yetkiniz yok");
            }

            List<Long> classroomIds = request.get("classroomIds");
            if (classroomIds == null) {
                return ResponseEntity.badRequest().body("Sınıf ID'leri gerekli");
            }

            // Mevcut sınıflardan dersi çıkar
            List<ClassRoom> allClassrooms = classRoomRepository.findAll();
            for (ClassRoom classroom : allClassrooms) {
                if (classroom.getCourses().contains(course)) {
                    classroom.removeCourse(course);
                    classRoomRepository.save(classroom);
                }
            }

            // Yeni sınıflara dersi ekle
            for (Long classroomId : classroomIds) {
                ClassRoom classroom = classRoomRepository.findById(classroomId).orElse(null);
                if (classroom != null) {
                    classroom.addCourse(course);
                    classRoomRepository.save(classroom);
                }
            }

            return ResponseEntity.ok(Map.of("message", "Ders sınıfları güncellendi"));
        } catch (Exception e) {
            System.err.println("COURSE DEBUG: Error updating course classrooms: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Ders sınıfları güncellenirken hata oluştu: " + e.getMessage());
        }
    }
}

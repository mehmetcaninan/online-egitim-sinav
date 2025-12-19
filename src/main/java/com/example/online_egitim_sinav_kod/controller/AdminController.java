package com.example.online_egitim_sinav_kod.controller;

import com.example.online_egitim_sinav_kod.model.ClassRoom;
import com.example.online_egitim_sinav_kod.model.Course;
import com.example.online_egitim_sinav_kod.model.CourseEnrollment;
import com.example.online_egitim_sinav_kod.model.Role;
import com.example.online_egitim_sinav_kod.model.User;
import com.example.online_egitim_sinav_kod.repository.ClassRoomRepository;
import com.example.online_egitim_sinav_kod.repository.CourseEnrollmentRepository;
import com.example.online_egitim_sinav_kod.repository.CourseRepository;
import com.example.online_egitim_sinav_kod.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"}, allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE}, allowCredentials = "true")
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final ClassRoomRepository classRoomRepository;
    private final CourseEnrollmentRepository courseEnrollmentRepository;
    private final CourseRepository courseRepository;

    public AdminController(UserRepository userRepository, 
                          ClassRoomRepository classRoomRepository,
                          CourseEnrollmentRepository courseEnrollmentRepository,
                          CourseRepository courseRepository) {
        this.userRepository = userRepository;
        this.classRoomRepository = classRoomRepository;
        this.courseEnrollmentRepository = courseEnrollmentRepository;
        this.courseRepository = courseRepository;
    }

    @GetMapping("/users")
    public ResponseEntity<?> listUsers() {
        try {
            System.out.println("ADMIN DEBUG: Fetching all users...");
            List<User> users = userRepository.findAll();
            System.out.println("ADMIN DEBUG: Found " + users.size() + " users");
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            System.out.println("ADMIN DEBUG: Error fetching users: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Kullanıcılar alınırken hata oluştu: " + e.getMessage()));
        }
    }

    @PostMapping("/users/{id}/role")
    public ResponseEntity<?> setRole(@PathVariable Long id, @RequestParam Role role) {
        try {
            System.out.println("ADMIN DEBUG: Setting role " + role + " for user ID: " + id);

            Optional<User> userOpt = userRepository.findById(id);
            if (!userOpt.isPresent()) {
                System.out.println("ADMIN DEBUG: User not found with ID: " + id);
                return ResponseEntity.status(404).body(Map.of("error", "Kullanıcı bulunamadı"));
            }

            User user = userOpt.get();
            System.out.println("ADMIN DEBUG: Current user details: " + user);
            user.setRole(role);
            user.setApproved(true); // Rol değişikliği yapıldığında otomatik olarak onayla
            User savedUser = userRepository.save(user);

            System.out.println("ADMIN DEBUG: User role updated successfully. ID: " + id + ", New role: " + role + ", Approved: " + savedUser.isApproved());
            return ResponseEntity.ok(savedUser);

        } catch (Exception e) {
            System.out.println("ADMIN DEBUG: Error setting role: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Rol güncellenirken hata oluştu: " + e.getMessage()));
        }
    }

    @PostMapping("/users/{id}/approve")
    public ResponseEntity<?> approveUser(@PathVariable Long id, @RequestParam(required = false) Long classroomId) {
        try {
            System.out.println("ADMIN DEBUG: Approving user ID: " + id + ", Classroom ID: " + classroomId);

            Optional<User> userOpt = userRepository.findById(id);
            if (!userOpt.isPresent()) {
                System.out.println("ADMIN DEBUG: User not found with ID: " + id);
                return ResponseEntity.status(404).body(Map.of("error", "Kullanıcı bulunamadı"));
            }

            User user = userOpt.get();
            user.setApproved(true);
            user.setRejected(false); // Onaylandığında reddedilmiş işaretini kaldır
            
            // Eğer öğrenci ise ve sınıf ID verilmişse, öğrenciyi sınıfa ata
            if (user.getRole() == Role.STUDENT && classroomId != null) {
                Optional<ClassRoom> classroomOpt = classRoomRepository.findById(classroomId);
                if (classroomOpt.isPresent()) {
                    ClassRoom classroom = classroomOpt.get();
                    classroom.addStudent(user);
                    classRoomRepository.save(classroom);
                    System.out.println("ADMIN DEBUG: Student assigned to classroom: " + classroom.getName());
                }
            }
            
            User savedUser = userRepository.save(user);

            System.out.println("ADMIN DEBUG: User approved successfully. ID: " + id + ", Username: " + user.getUsername());
            return ResponseEntity.ok(savedUser);

        } catch (Exception e) {
            System.out.println("ADMIN DEBUG: Error approving user: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Kullanıcı onaylanırken hata oluştu: " + e.getMessage()));
        }
    }

    @PostMapping("/users/{id}/reject")
    public ResponseEntity<?> rejectUser(@PathVariable Long id) {
        try {
            System.out.println("ADMIN DEBUG: Rejecting user ID: " + id);

            Optional<User> userOpt = userRepository.findById(id);
            if (!userOpt.isPresent()) {
                System.out.println("ADMIN DEBUG: User not found with ID: " + id);
                return ResponseEntity.status(404).body(Map.of("error", "Kullanıcı bulunamadı"));
            }

            User user = userOpt.get();
            // Kullanıcıyı reddedildi olarak işaretle
            user.setApproved(false);
            user.setRejected(true);
            User savedUser = userRepository.save(user);

            System.out.println("ADMIN DEBUG: User rejected successfully. ID: " + id + ", Username: " + user.getUsername());
            return ResponseEntity.ok(Map.of("message", "Kullanıcı reddedildi", "user", savedUser));

        } catch (Exception e) {
            System.out.println("ADMIN DEBUG: Error rejecting user: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Kullanıcı reddedilirken hata oluştu: " + e.getMessage()));
        }
    }

    // Sınıf yönetimi endpoint'leri
    @GetMapping("/classrooms")
    public ResponseEntity<?> listClassrooms() {
        try {
            // Sadece aktif sınıfları getir
            List<ClassRoom> allClassrooms = classRoomRepository.findAll();
            List<ClassRoom> activeClassrooms = allClassrooms.stream()
                .filter(c -> c.isActive())
                .collect(Collectors.toList());
            return ResponseEntity.ok(activeClassrooms);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Sınıflar alınırken hata oluştu: " + e.getMessage()));
        }
    }

    @PostMapping("/classrooms")
    public ResponseEntity<?> createClassroom(@RequestBody Map<String, String> request) {
        try {
            String name = request.get("name");
            String description = request.get("description");

            if (name == null || name.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Sınıf adı zorunludur"));
            }

            // Aynı isimde sınıf var mı kontrol et
            if (classRoomRepository.findByName(name).isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Bu isimde bir sınıf zaten mevcut"));
            }

            ClassRoom classroom = new ClassRoom(name, description);
            ClassRoom savedClassroom = classRoomRepository.save(classroom);

            return ResponseEntity.ok(savedClassroom);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Sınıf oluşturulurken hata oluştu: " + e.getMessage()));
        }
    }

    @GetMapping("/classrooms/{id}")
    public ResponseEntity<?> getClassroom(@PathVariable Long id) {
        try {
            Optional<ClassRoom> classroomOpt = classRoomRepository.findById(id);
            if (classroomOpt.isPresent()) {
                return ResponseEntity.ok(classroomOpt.get());
            } else {
                return ResponseEntity.status(404).body(Map.of("error", "Sınıf bulunamadı"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Sınıf alınırken hata oluştu: " + e.getMessage()));
        }
    }

    @PostMapping("/classrooms/{classroomId}/students/{studentId}")
    public ResponseEntity<?> assignStudentToClassroom(@PathVariable Long classroomId, @PathVariable Long studentId) {
        try {
            Optional<ClassRoom> classroomOpt = classRoomRepository.findById(classroomId);
            Optional<User> studentOpt = userRepository.findById(studentId);

            if (!classroomOpt.isPresent()) {
                return ResponseEntity.status(404).body(Map.of("error", "Sınıf bulunamadı"));
            }

            if (!studentOpt.isPresent()) {
                return ResponseEntity.status(404).body(Map.of("error", "Öğrenci bulunamadı"));
            }

            User student = studentOpt.get();
            if (student.getRole() != Role.STUDENT) {
                return ResponseEntity.badRequest().body(Map.of("error", "Sadece öğrenciler sınıfa atanabilir"));
            }

            ClassRoom classroom = classroomOpt.get();
            classroom.addStudent(student);
            classRoomRepository.save(classroom);

            return ResponseEntity.ok(Map.of("message", "Öğrenci sınıfa atandı", "classroom", classroom));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Öğrenci atanırken hata oluştu: " + e.getMessage()));
        }
    }

    @DeleteMapping("/classrooms/{classroomId}/students/{studentId}")
    public ResponseEntity<?> removeStudentFromClassroom(@PathVariable Long classroomId, @PathVariable Long studentId) {
        try {
            Optional<ClassRoom> classroomOpt = classRoomRepository.findById(classroomId);
            Optional<User> studentOpt = userRepository.findById(studentId);

            if (!classroomOpt.isPresent()) {
                return ResponseEntity.status(404).body(Map.of("error", "Sınıf bulunamadı"));
            }

            if (!studentOpt.isPresent()) {
                return ResponseEntity.status(404).body(Map.of("error", "Öğrenci bulunamadı"));
            }

            ClassRoom classroom = classroomOpt.get();
            classroom.removeStudent(studentOpt.get());
            classRoomRepository.save(classroom);

            return ResponseEntity.ok(Map.of("message", "Öğrenci sınıftan çıkarıldı"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Öğrenci çıkarılırken hata oluştu: " + e.getMessage()));
        }
    }

    @GetMapping("/classrooms/{id}/students")
    public ResponseEntity<?> getClassroomStudents(@PathVariable Long id) {
        try {
            Optional<ClassRoom> classroomOpt = classRoomRepository.findById(id);
            if (classroomOpt.isPresent()) {
                ClassRoom classroom = classroomOpt.get();
                return ResponseEntity.ok(classroom.getStudents());
            } else {
                return ResponseEntity.status(404).body(Map.of("error", "Sınıf bulunamadı"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Öğrenciler alınırken hata oluştu: " + e.getMessage()));
        }
    }

    // Belirli bir sınıfa ait dersleri getir
    @GetMapping("/classrooms/{id}/courses")
    public ResponseEntity<?> getClassroomCourses(@PathVariable Long id) {
        try {
            Optional<ClassRoom> classroomOpt = classRoomRepository.findById(id);
            if (classroomOpt.isPresent()) {
                ClassRoom classroom = classroomOpt.get();
                return ResponseEntity.ok(classroom.getCourses());
            } else {
                return ResponseEntity.status(404).body(Map.of("error", "Sınıf bulunamadı"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Dersler alınırken hata oluştu: " + e.getMessage()));
        }
    }

    // Sınıf güncelleme
    @PutMapping("/classrooms/{id}")
    public ResponseEntity<?> updateClassroom(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            Optional<ClassRoom> classroomOpt = classRoomRepository.findById(id);
            if (!classroomOpt.isPresent()) {
                return ResponseEntity.status(404).body(Map.of("error", "Sınıf bulunamadı"));
            }

            ClassRoom classroom = classroomOpt.get();
            String name = request.get("name");
            String description = request.get("description");

            if (name != null && !name.trim().isEmpty()) {
                // Aynı isimde başka bir sınıf var mı kontrol et
                Optional<ClassRoom> existingClassroom = classRoomRepository.findByName(name);
                if (existingClassroom.isPresent() && !existingClassroom.get().getId().equals(id)) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Bu isimde bir sınıf zaten mevcut"));
                }
                classroom.setName(name);
            }

            if (description != null) {
                classroom.setDescription(description);
            }

            ClassRoom savedClassroom = classRoomRepository.save(classroom);
            return ResponseEntity.ok(savedClassroom);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Sınıf güncellenirken hata oluştu: " + e.getMessage()));
        }
    }

    // Sınıf silme
    @DeleteMapping("/classrooms/{id}")
    public ResponseEntity<?> deleteClassroom(@PathVariable Long id) {
        try {
            Optional<ClassRoom> classroomOpt = classRoomRepository.findById(id);
            if (!classroomOpt.isPresent()) {
                return ResponseEntity.status(404).body(Map.of("error", "Sınıf bulunamadı"));
            }

            ClassRoom classroom = classroomOpt.get();
            // Sınıfı pasif yap (soft delete)
            classroom.setActive(false);
            classRoomRepository.save(classroom);

            return ResponseEntity.ok(Map.of("message", "Sınıf silindi"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Sınıf silinirken hata oluştu: " + e.getMessage()));
        }
    }

    // Kullanıcı güncelleme
    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        try {
            Optional<User> userOpt = userRepository.findById(id);
            if (!userOpt.isPresent()) {
                return ResponseEntity.status(404).body(Map.of("error", "Kullanıcı bulunamadı"));
            }

            User user = userOpt.get();
            
            if (request.containsKey("fullName")) {
                user.setFullName((String) request.get("fullName"));
            }
            
            if (request.containsKey("username")) {
                String newUsername = (String) request.get("username");
                // Aynı username'de başka bir kullanıcı var mı kontrol et
                Optional<User> existingUser = userRepository.findByUsername(newUsername);
                if (existingUser.isPresent() && !existingUser.get().getId().equals(id)) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Bu kullanıcı adı zaten kullanılıyor"));
                }
                user.setUsername(newUsername);
            }

            if (request.containsKey("role")) {
                String roleStr = (String) request.get("role");
                try {
                    Role role = Role.valueOf(roleStr);
                    user.setRole(role);
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Geçersiz rol"));
                }
            }

            User savedUser = userRepository.save(user);
            return ResponseEntity.ok(savedUser);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Kullanıcı güncellenirken hata oluştu: " + e.getMessage()));
        }
    }

    // Kullanıcı silme
    @DeleteMapping("/users/{id}")
    @Transactional
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            Optional<User> userOpt = userRepository.findById(id);
            if (!userOpt.isPresent()) {
                return ResponseEntity.status(404).body(Map.of("error", "Kullanıcı bulunamadı"));
            }

            User user = userOpt.get();
            
            System.out.println("ADMIN DEBUG: Deleting user ID: " + user.getId() + ", Username: " + user.getUsername());
            
            // 1. Kullanıcıya ait tüm CourseEnrollment kayıtlarını sil
            List<CourseEnrollment> enrollments = courseEnrollmentRepository.findByUserId(user.getId());
            System.out.println("ADMIN DEBUG: Found " + enrollments.size() + " enrollments for user " + user.getId());
            
            if (!enrollments.isEmpty()) {
                courseEnrollmentRepository.deleteAll(enrollments);
                courseEnrollmentRepository.flush(); // Hemen veritabanına yaz
                System.out.println("ADMIN DEBUG: Deleted all enrollments");
            }
            
            // 2. Eğer öğrenci ise, tüm sınıflardan çıkar
            if (user.getRole() == Role.STUDENT) {
                List<ClassRoom> classrooms = classRoomRepository.findAll();
                for (ClassRoom classroom : classrooms) {
                    if (classroom.getStudents().contains(user)) {
                        classroom.removeStudent(user);
                        classRoomRepository.save(classroom);
                    }
                }
                System.out.println("ADMIN DEBUG: Removed student from all classrooms");
            }
            
            // 3. Eğer öğretmen ise, onun derslerini pasif yap (veya silebiliriz)
            if (user.getRole() == Role.TEACHER) {
                List<Course> teacherCourses = courseRepository.findByTeacherId(user.getId());
                System.out.println("ADMIN DEBUG: Found " + teacherCourses.size() + " courses for teacher");
                for (Course course : teacherCourses) {
                    // Dersi pasif yap
                    course.setActive(false);
                    courseRepository.save(course);
                    
                    // Dersi tüm sınıflardan çıkar
                    List<ClassRoom> allClassrooms = classRoomRepository.findAll();
                    for (ClassRoom classroom : allClassrooms) {
                        if (classroom.getCourses().contains(course)) {
                            classroom.removeCourse(course);
                            classRoomRepository.save(classroom);
                        }
                    }
                }
                System.out.println("ADMIN DEBUG: Deactivated all teacher courses");
            }

            // 4. Son olarak kullanıcıyı sil
            userRepository.delete(user);
            userRepository.flush(); // Hemen veritabanına yaz
            System.out.println("ADMIN DEBUG: User deleted successfully");
            
            return ResponseEntity.ok(Map.of("message", "Kullanıcı ve ilgili tüm kayıtlar silindi"));
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("ADMIN DEBUG: Error deleting user: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Kullanıcı silinirken hata oluştu: " + e.getMessage()));
        }
    }

    // Öğrenci sınıf değiştirme
    @PutMapping("/users/{userId}/classroom")
    public ResponseEntity<?> changeStudentClassroom(@PathVariable Long userId, @RequestBody Map<String, Long> request) {
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (!userOpt.isPresent()) {
                return ResponseEntity.status(404).body(Map.of("error", "Kullanıcı bulunamadı"));
            }

            User user = userOpt.get();
            if (user.getRole() != Role.STUDENT) {
                return ResponseEntity.badRequest().body(Map.of("error", "Sadece öğrenciler için sınıf değiştirilebilir"));
            }

            Long newClassroomId = request.get("classroomId");
            if (newClassroomId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Sınıf ID gerekli"));
            }

            Optional<ClassRoom> newClassroomOpt = classRoomRepository.findById(newClassroomId);
            if (!newClassroomOpt.isPresent()) {
                return ResponseEntity.status(404).body(Map.of("error", "Sınıf bulunamadı"));
            }

            // Öğrenciyi eski sınıflardan çıkar
            List<ClassRoom> allClassrooms = classRoomRepository.findAll();
            for (ClassRoom classroom : allClassrooms) {
                if (classroom.getStudents().contains(user)) {
                    classroom.removeStudent(user);
                    classRoomRepository.save(classroom);
                }
            }

            // Öğrenciyi yeni sınıfa ekle
            ClassRoom newClassroom = newClassroomOpt.get();
            newClassroom.addStudent(user);
            classRoomRepository.save(newClassroom);

            return ResponseEntity.ok(Map.of("message", "Öğrenci sınıfı değiştirildi", "classroom", newClassroom));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Sınıf değiştirilirken hata oluştu: " + e.getMessage()));
        }
    }

    // Manuel kullanıcı silme - username ile (sorunlu kullanıcılar için)
    @DeleteMapping("/users/by-username/{username}")
    @Transactional
    public ResponseEntity<?> deleteUserByUsername(@PathVariable String username) {
        try {
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (!userOpt.isPresent()) {
                return ResponseEntity.status(404).body(Map.of("error", "Kullanıcı bulunamadı: " + username));
            }

            User user = userOpt.get();
            Long userId = user.getId();
            
            System.out.println("ADMIN DEBUG: Force deleting user by username: " + username + ", ID: " + userId);
            
            // 1. Kullanıcıya ait tüm CourseEnrollment kayıtlarını sil
            List<CourseEnrollment> enrollments = courseEnrollmentRepository.findByUserId(userId);
            System.out.println("ADMIN DEBUG: Found " + enrollments.size() + " enrollments");
            
            if (!enrollments.isEmpty()) {
                // Native SQL ile direkt silme (foreign key constraint'i bypass etmek için)
                courseEnrollmentRepository.deleteAll(enrollments);
                courseEnrollmentRepository.flush();
                System.out.println("ADMIN DEBUG: Deleted all enrollments");
            }
            
            // 2. Öğrenci ise sınıflardan çıkar
            if (user.getRole() == Role.STUDENT) {
                List<ClassRoom> classrooms = classRoomRepository.findAll();
                for (ClassRoom classroom : classrooms) {
                    if (classroom.getStudents().contains(user)) {
                        classroom.removeStudent(user);
                        classRoomRepository.save(classroom);
                    }
                }
                System.out.println("ADMIN DEBUG: Removed from classrooms");
            }
            
            // 3. Öğretmen ise derslerini pasif yap
            if (user.getRole() == Role.TEACHER) {
                List<Course> courses = courseRepository.findByTeacherId(userId);
                for (Course course : courses) {
                    course.setActive(false);
                    courseRepository.save(course);
                }
                System.out.println("ADMIN DEBUG: Deactivated courses");
            }

            // 4. Kullanıcıyı sil
            userRepository.delete(user);
            userRepository.flush();
            System.out.println("ADMIN DEBUG: User deleted successfully");
            
            return ResponseEntity.ok(Map.of("message", "Kullanıcı başarıyla silindi: " + username));
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("ADMIN DEBUG: Error deleting user by username: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Kullanıcı silinirken hata oluştu: " + e.getMessage()));
        }
    }
}

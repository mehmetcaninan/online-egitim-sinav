package com.example.online_egitim_sinav_kod.controller;

import com.example.online_egitim_sinav_kod.dto.EnrollmentRequestDTO;
import com.example.online_egitim_sinav_kod.service.CourseEnrollmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teacher/enrollments")
@CrossOrigin(origins = "http://localhost:5173")
public class TeacherEnrollmentController {

    @Autowired
    private CourseEnrollmentService enrollmentService;

    // Bekleyen katılım isteklerini getir
    @GetMapping("/pending/{teacherId}")
    public ResponseEntity<List<EnrollmentRequestDTO>> getPendingEnrollments(@PathVariable Long teacherId) {
        try {
            List<EnrollmentRequestDTO> pendingRequests = enrollmentService.getPendingEnrollmentRequests(teacherId);
            return ResponseEntity.ok(pendingRequests);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Tüm katılım isteklerini getir
    @GetMapping("/all/{teacherId}")
    public ResponseEntity<List<EnrollmentRequestDTO>> getAllEnrollments(@PathVariable Long teacherId) {
        try {
            List<EnrollmentRequestDTO> allRequests = enrollmentService.getAllEnrollmentRequests(teacherId);
            return ResponseEntity.ok(allRequests);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Katılım isteğini onayla
    @PostMapping("/approve/{enrollmentId}")
    public ResponseEntity<String> approveEnrollment(@PathVariable Long enrollmentId) {
        try {
            boolean success = enrollmentService.approveEnrollment(enrollmentId);
            if (success) {
                return ResponseEntity.ok("Katılım isteği onaylandı");
            } else {
                return ResponseEntity.badRequest().body("Katılım isteği onaylanamadı");
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Bir hata oluştu: " + e.getMessage());
        }
    }

    // Katılım isteğini reddet
    @PostMapping("/reject/{enrollmentId}")
    public ResponseEntity<String> rejectEnrollment(@PathVariable Long enrollmentId) {
        try {
            boolean success = enrollmentService.rejectEnrollment(enrollmentId);
            if (success) {
                return ResponseEntity.ok("Katılım isteği reddedildi");
            } else {
                return ResponseEntity.badRequest().body("Katılım isteği reddedilemedi");
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Bir hata oluştu: " + e.getMessage());
        }
    }
}

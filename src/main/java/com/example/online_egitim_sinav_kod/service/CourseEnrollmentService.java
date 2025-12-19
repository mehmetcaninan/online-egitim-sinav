package com.example.online_egitim_sinav_kod.service;

import com.example.online_egitim_sinav_kod.dto.EnrollmentRequestDTO;
import com.example.online_egitim_sinav_kod.model.CourseEnrollment;
import com.example.online_egitim_sinav_kod.model.CourseEnrollment.EnrollmentStatus;
import com.example.online_egitim_sinav_kod.repository.CourseEnrollmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseEnrollmentService {

    @Autowired
    private CourseEnrollmentRepository enrollmentRepository;

    // Öğretmenin kurslarına ait bekleyen katılım isteklerini getir
    public List<EnrollmentRequestDTO> getPendingEnrollmentRequests(Long teacherId) {
        List<CourseEnrollment> pendingEnrollments = enrollmentRepository
            .findByTeacherIdAndStatus(teacherId, EnrollmentStatus.PENDING);

        return pendingEnrollments.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    // Öğretmenin tüm katılım isteklerini getir (bekleyen, onaylanan, reddedilen)
    public List<EnrollmentRequestDTO> getAllEnrollmentRequests(Long teacherId) {
        List<CourseEnrollment> allEnrollments = enrollmentRepository.findByTeacherId(teacherId);

        return allEnrollments.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    // Katılım isteğini onayla
    public boolean approveEnrollment(Long enrollmentId) {
        try {
            CourseEnrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new RuntimeException("Katılım isteği bulunamadı"));

            enrollment.setStatus(EnrollmentStatus.APPROVED);
            enrollment.setRespondedAt(Instant.now());
            enrollmentRepository.save(enrollment);

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Katılım isteğini reddet
    public boolean rejectEnrollment(Long enrollmentId) {
        try {
            CourseEnrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new RuntimeException("Katılım isteği bulunamadı"));

            enrollment.setStatus(EnrollmentStatus.REJECTED);
            enrollment.setRespondedAt(Instant.now());
            enrollmentRepository.save(enrollment);

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // CourseEnrollment'i DTO'ya çevir
    private EnrollmentRequestDTO convertToDTO(CourseEnrollment enrollment) {
        EnrollmentRequestDTO dto = new EnrollmentRequestDTO();
        dto.setId(enrollment.getId());
        dto.setUserId(enrollment.getUser().getId());
        dto.setUserName(enrollment.getUser().getUsername());
        dto.setUserFullName(enrollment.getUser().getFullName());
        dto.setCourseId(enrollment.getCourse().getId());
        dto.setCourseTitle(enrollment.getCourse().getTitle());
        dto.setStatus(enrollment.getStatus());
        dto.setMessage(enrollment.getMessage());
        dto.setRequestedAt(enrollment.getRequestedAt());
        dto.setRespondedAt(enrollment.getRespondedAt());

        return dto;
    }
}

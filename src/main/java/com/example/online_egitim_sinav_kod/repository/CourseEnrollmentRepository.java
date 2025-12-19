package com.example.online_egitim_sinav_kod.repository;

import com.example.online_egitim_sinav_kod.model.CourseEnrollment;
import com.example.online_egitim_sinav_kod.model.CourseEnrollment.EnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseEnrollmentRepository extends JpaRepository<CourseEnrollment, Long> {

    @Query("SELECT ce FROM CourseEnrollment ce WHERE ce.course.teacher.id = :teacherId AND ce.status = :status")
    List<CourseEnrollment> findByTeacherIdAndStatus(@Param("teacherId") Long teacherId, @Param("status") EnrollmentStatus status);

    @Query("SELECT ce FROM CourseEnrollment ce WHERE ce.course.teacher.id = :teacherId")
    List<CourseEnrollment> findByTeacherId(@Param("teacherId") Long teacherId);

    List<CourseEnrollment> findByCourseIdAndStatus(Long courseId, EnrollmentStatus status);

    List<CourseEnrollment> findByUserIdAndCourseId(Long userId, Long courseId);

    // Düzeltilmiş query metodları - field adlarını model ile uyumlu hale getirdim
    @Query("SELECT ce FROM CourseEnrollment ce WHERE ce.user.id = :userId AND ce.course.id = :courseId")
    List<CourseEnrollment> findByStudentIdAndCourseId(@Param("userId") Long studentId, @Param("courseId") Long courseId);

    @Query("SELECT ce FROM CourseEnrollment ce WHERE ce.user.id = :userId ORDER BY ce.requestedAt DESC")
    List<CourseEnrollment> findByStudentIdOrderByRequestDateDesc(@Param("userId") Long studentId);

    @Query("SELECT ce FROM CourseEnrollment ce WHERE ce.course.teacher.id = :teacherId AND ce.status = :status ORDER BY ce.requestedAt DESC")
    List<CourseEnrollment> findByCourseTeacherIdAndStatusOrderByRequestDateDesc(@Param("teacherId") Long teacherId, @Param("status") EnrollmentStatus status);

    @Query("SELECT ce FROM CourseEnrollment ce WHERE ce.user.id = :userId AND ce.status = :status")
    List<CourseEnrollment> findByStudentIdAndStatus(@Param("userId") Long studentId, @Param("status") EnrollmentStatus status);

    // Kullanıcıya ait tüm enrollment'ları bul
    @Query("SELECT ce FROM CourseEnrollment ce WHERE ce.user.id = :userId")
    List<CourseEnrollment> findByUserId(@Param("userId") Long userId);
}

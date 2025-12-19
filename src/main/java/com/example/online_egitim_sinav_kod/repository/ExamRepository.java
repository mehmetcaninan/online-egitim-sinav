package com.example.online_egitim_sinav_kod.repository;

import com.example.online_egitim_sinav_kod.model.Exam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ExamRepository extends JpaRepository<Exam, Long> {
    @Query("SELECT e FROM Exam e WHERE e.course.id = :courseId")
    List<Exam> findByCourseId(@Param("courseId") Long courseId);
}


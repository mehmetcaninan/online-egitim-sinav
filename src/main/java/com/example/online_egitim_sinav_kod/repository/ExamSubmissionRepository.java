package com.example.online_egitim_sinav_kod.repository;

import com.example.online_egitim_sinav_kod.model.ExamSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamSubmissionRepository extends JpaRepository<ExamSubmission, Long> {
    List<ExamSubmission> findByExamId(Long examId);
    List<ExamSubmission> findByStudentId(Long studentId);
    Optional<ExamSubmission> findByExamIdAndStudentId(Long examId, Long studentId);
}


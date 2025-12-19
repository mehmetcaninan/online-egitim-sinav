package com.example.online_egitim_sinav_kod.repository;

import com.example.online_egitim_sinav_kod.model.Submission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
}


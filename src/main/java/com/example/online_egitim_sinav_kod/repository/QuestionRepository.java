package com.example.online_egitim_sinav_kod.repository;

import com.example.online_egitim_sinav_kod.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByExamIdOrderByIdAsc(Long examId);
}


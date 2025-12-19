package com.example.online_egitim_sinav_kod.repository;

import com.example.online_egitim_sinav_kod.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByTeacherId(Long teacherId);
    List<Course> findByActive(boolean active);
}

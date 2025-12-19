package com.example.online_egitim_sinav_kod.repository;

import com.example.online_egitim_sinav_kod.model.ResourceFile;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResourceFileRepository extends JpaRepository<ResourceFile, Long> {
    @Query("SELECT rf FROM ResourceFile rf WHERE rf.course.id = :courseId ORDER BY rf.uploadedAt DESC")
    List<ResourceFile> findByCourseIdOrderByUploadedAtDesc(@Param("courseId") Long courseId);
}


package com.example.online_egitim_sinav_kod.repository;

import com.example.online_egitim_sinav_kod.model.VideoResource;
import com.example.online_egitim_sinav_kod.model.Course;
import com.example.online_egitim_sinav_kod.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VideoResourceRepository extends JpaRepository<VideoResource, Long> {

    List<VideoResource> findByCourse(Course course);

    List<VideoResource> findByCourseId(Long courseId);

    List<VideoResource> findByUploadedBy(User uploadedBy);

    List<VideoResource> findByCourseIdOrderByCreatedAtDesc(Long courseId);

    List<VideoResource> findByUploadedByOrderByCreatedAtDesc(User uploadedBy);

    long countByCourse(Course course);
}

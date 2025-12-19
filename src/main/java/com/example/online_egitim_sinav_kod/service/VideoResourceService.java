package com.example.online_egitim_sinav_kod.service;

import com.example.online_egitim_sinav_kod.model.VideoResource;
import com.example.online_egitim_sinav_kod.model.Course;
import com.example.online_egitim_sinav_kod.model.User;
import com.example.online_egitim_sinav_kod.repository.VideoResourceRepository;
import com.example.online_egitim_sinav_kod.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class VideoResourceService {

    @Autowired
    private VideoResourceRepository videoResourceRepository;

    @Autowired
    private CourseRepository courseRepository;

    public List<VideoResource> getAllVideos() {
        return videoResourceRepository.findAll();
    }

    public Optional<VideoResource> getVideoById(Long id) {
        return videoResourceRepository.findById(id);
    }

    public List<VideoResource> getVideosByCourse(Long courseId) {
        return videoResourceRepository.findByCourseIdOrderByCreatedAtDesc(courseId);
    }

    public List<VideoResource> getVideosByTeacher(User teacher) {
        return videoResourceRepository.findByUploadedByOrderByCreatedAtDesc(teacher);
    }

    public VideoResource createVideo(VideoResource videoResource) {
        return videoResourceRepository.save(videoResource);
    }

    public VideoResource updateVideo(Long id, VideoResource videoDetails) {
        Optional<VideoResource> optionalVideo = videoResourceRepository.findById(id);
        if (optionalVideo.isPresent()) {
            VideoResource video = optionalVideo.get();
            video.setTitle(videoDetails.getTitle());
            video.setDescription(videoDetails.getDescription());
            video.setVideoUrl(videoDetails.getVideoUrl());
            video.setVideoType(videoDetails.getVideoType());
            return videoResourceRepository.save(video);
        }
        return null;
    }

    public boolean deleteVideo(Long id) {
        if (videoResourceRepository.existsById(id)) {
            videoResourceRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public long getVideoCountByCourse(Course course) {
        return videoResourceRepository.countByCourse(course);
    }

    public boolean canUserAccessVideo(VideoResource video, User user) {
        try {
            if (video == null || user == null) {
                return false;
            }

            // Eğer öğretmen ise veya video sahibiyse erişebilir
            if (user.getRole().name().equals("TEACHER") ||
                (video.getUploadedBy() != null && video.getUploadedBy().getId().equals(user.getId()))) {
                return true;
            }

            // Eğer öğrenci ise ve derse kayıtlıysa erişebilir
            if (user.getRole().name().equals("STUDENT")) {
                // Basit kontrol - gerçek uygulamada CourseEnrollmentService kullanılabilir
                return true;
            }

            return false;
        } catch (Exception e) {
            System.err.println("Error in canUserAccessVideo: " + e.getMessage());
            return false;
        }
    }
}

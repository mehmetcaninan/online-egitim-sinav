package com.example.online_egitim_sinav_kod.controller;

import com.example.online_egitim_sinav_kod.model.VideoResource;
import com.example.online_egitim_sinav_kod.model.Course;
import com.example.online_egitim_sinav_kod.model.User;
import com.example.online_egitim_sinav_kod.service.VideoResourceService;
import com.example.online_egitim_sinav_kod.service.CourseService;
import com.example.online_egitim_sinav_kod.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/videos")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class VideoResourceController {

    @Autowired
    private VideoResourceService videoResourceService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<List<VideoResource>> getAllVideos() {
        List<VideoResource> videos = videoResourceService.getAllVideos();
        return ResponseEntity.ok(videos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VideoResource> getVideoById(@PathVariable Long id, Authentication auth) {
        Optional<VideoResource> video = videoResourceService.getVideoById(id);
        if (video.isPresent()) {
            User user = userService.findByUsername(auth.getName());
            if (videoResourceService.canUserAccessVideo(video.get(), user)) {
                return ResponseEntity.ok(video.get());
            } else {
                return ResponseEntity.status(403).build();
            }
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<VideoResource>> getVideosByCourse(@PathVariable Long courseId) {
        List<VideoResource> videos = videoResourceService.getVideosByCourse(courseId);
        return ResponseEntity.ok(videos);
    }

    @GetMapping("/my-videos")
    public ResponseEntity<List<VideoResource>> getMyVideos(Authentication auth) {
        try {
            // Geçici olarak hardcoded teacher kullanıcısı ile çalıştıralım
            User user = userService.findByUsername("ogretmen");
            if (user == null) {
                // Eğer ogretmen kullanıcısı yoksa, TEACHER rolündeki ilk kullanıcıyı alalım
                user = userService.findFirstByRole("TEACHER");
            }

            if (user == null) {
                return ResponseEntity.status(500).build();
            }

            List<VideoResource> videos = videoResourceService.getVideosByTeacher(user);
            return ResponseEntity.ok(videos != null ? videos : List.of());
        } catch (Exception e) {
            System.err.println("Error in getMyVideos: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping
    public ResponseEntity<VideoResource> createVideo(@RequestBody VideoResource videoResource, Authentication auth) {
        try {
            System.out.println("DEBUG: createVideo called with: " + (videoResource != null ? videoResource.getTitle() : "null"));

            // Geçici olarak hardcoded teacher kullanıcısı ile çalıştıralım
            User user = userService.findByUsername("ogretmen");
            if (user == null) {
                // Eğer ogretmen kullanıcısı yoksa, TEACHER rolündeki ilk kullanıcıyı alalım
                user = userService.findFirstByRole("TEACHER");
            }

            if (user == null) {
                System.err.println("ERROR: No teacher user found");
                return ResponseEntity.status(500).build();
            }

            // Temel alanları kontrol et
            if (videoResource == null || videoResource.getTitle() == null || videoResource.getTitle().trim().isEmpty()) {
                System.err.println("ERROR: Video title is null or empty");
                return ResponseEntity.badRequest().body(null);
            }

            if (videoResource.getVideoUrl() == null || videoResource.getVideoUrl().trim().isEmpty()) {
                System.err.println("ERROR: Video URL is null or empty");
                return ResponseEntity.badRequest().body(null);
            }

            videoResource.setUploadedBy(user);

            // Ders kontrolü
            if (videoResource.getCourse() != null && videoResource.getCourse().getId() != null) {
                Course course = courseService.getCourseById(videoResource.getCourse().getId());
                if (course != null) {
                    videoResource.setCourse(course);
                    System.out.println("DEBUG: Course set: " + course.getTitle());
                } else {
                    System.err.println("ERROR: Course not found with id: " + videoResource.getCourse().getId());
                    return ResponseEntity.badRequest().build();
                }
            } else {
                System.out.println("DEBUG: No course specified");
                videoResource.setCourse(null);
            }

            VideoResource createdVideo = videoResourceService.createVideo(videoResource);
            System.out.println("DEBUG: Video created successfully with id: " + createdVideo.getId());
            return ResponseEntity.ok(createdVideo);
        } catch (Exception e) {
            System.err.println("ERROR: Exception in createVideo: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<VideoResource> updateVideo(@PathVariable Long id, @RequestBody VideoResource videoResource, Authentication auth) {
        try {
            User user = userService.findByUsername(auth.getName());
            Optional<VideoResource> existingVideo = videoResourceService.getVideoById(id);

            if (existingVideo.isPresent()) {
                // Sadece video sahibi veya admin güncelleyebilir
                if (!existingVideo.get().getUploadedBy().getId().equals(user.getId()) &&
                    !user.getRole().name().equals("ADMIN")) {
                    return ResponseEntity.status(403).build();
                }

                VideoResource updatedVideo = videoResourceService.updateVideo(id, videoResource);
                if (updatedVideo != null) {
                    return ResponseEntity.ok(updatedVideo);
                }
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVideo(@PathVariable Long id, Authentication auth) {
        try {
            User user = userService.findByUsername(auth.getName());
            Optional<VideoResource> video = videoResourceService.getVideoById(id);

            if (video.isPresent()) {
                // Sadece video sahibi veya admin silebilir
                if (!video.get().getUploadedBy().getId().equals(user.getId()) &&
                    !user.getRole().name().equals("ADMIN")) {
                    return ResponseEntity.status(403).build();
                }

                boolean deleted = videoResourceService.deleteVideo(id);
                if (deleted) {
                    return ResponseEntity.ok().build();
                }
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}

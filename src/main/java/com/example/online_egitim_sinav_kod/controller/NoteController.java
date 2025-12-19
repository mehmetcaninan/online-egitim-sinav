package com.example.online_egitim_sinav_kod.controller;

import com.example.online_egitim_sinav_kod.model.Note;
import com.example.online_egitim_sinav_kod.model.ResourceFile;
import com.example.online_egitim_sinav_kod.model.User;
import com.example.online_egitim_sinav_kod.repository.CourseRepository;
import com.example.online_egitim_sinav_kod.repository.NoteRepository;
import com.example.online_egitim_sinav_kod.repository.ResourceFileRepository;
import com.example.online_egitim_sinav_kod.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final ResourceFileRepository resourceFileRepository;

    public NoteController(NoteRepository noteRepository, UserRepository userRepository, CourseRepository courseRepository, ResourceFileRepository resourceFileRepository) {
        this.noteRepository = noteRepository;
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.resourceFileRepository = resourceFileRepository;
    }

    public static class NoteRequest {
        public Long courseId;
        public Long resourceId;
        public String content;
    }

    @PostMapping
    public ResponseEntity<Note> createNote(@RequestBody NoteRequest req, Principal principal) {
        Note n = new Note();
        n.setContent(req.content);
        if (principal != null) {
            userRepository.findByUsername(principal.getName()).ifPresent(n::setAuthor);
        }
        if (req.courseId != null) {
            courseRepository.findById(req.courseId).ifPresent(n::setCourse);
        }
        if (req.resourceId != null) {
            resourceFileRepository.findById(req.resourceId).ifPresent(n::setResource);
        }
        Note saved = noteRepository.save(n);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Note>> listByUser(@PathVariable Long userId) {
        List<Note> list = noteRepository.findAll().stream().filter(n -> n.getAuthor() != null && n.getAuthor().getId().equals(userId)).toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<Note>> listByCourse(@PathVariable Long courseId) {
        List<Note> list = noteRepository.findAll().stream().filter(n -> n.getCourse() != null && n.getCourse().getId().equals(courseId)).toList();
        return ResponseEntity.ok(list);
    }

}


package com.example.online_egitim_sinav_kod.repository;

import com.example.online_egitim_sinav_kod.model.Note;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoteRepository extends JpaRepository<Note, Long> {
}


package com.example.online_egitim_sinav_kod.repository;

import com.example.online_egitim_sinav_kod.model.ClassRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClassRoomRepository extends JpaRepository<ClassRoom, Long> {
    Optional<ClassRoom> findByName(String name);
    List<ClassRoom> findByActive(boolean active);
    
    @Query("SELECT c FROM ClassRoom c JOIN c.students s WHERE s.id = :studentId")
    List<ClassRoom> findByStudentId(@Param("studentId") Long studentId);
}


package com.example.online_egitim_sinav_kod.dto;

import java.util.List;

public class CourseRequest {
    private String title;
    private String description;
    private Long teacherId;
    private List<Long> classroomIds; // Dersin atanacağı sınıflar

    // Constructors
    public CourseRequest() {}

    public CourseRequest(String title, String description, Long teacherId) {
        this.title = title;
        this.description = description;
        this.teacherId = teacherId;
    }

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
    public List<Long> getClassroomIds() { return classroomIds; }
    public void setClassroomIds(List<Long> classroomIds) { this.classroomIds = classroomIds; }
}

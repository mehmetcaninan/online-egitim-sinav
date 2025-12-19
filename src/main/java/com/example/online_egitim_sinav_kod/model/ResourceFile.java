package com.example.online_egitim_sinav_kod.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "resource_files")
public class ResourceFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;
    private String originalFileName;
    private String filePath;
    private String fileType;
    private Long fileSize;

    private String contentType;
    private String path;

    @Enumerated(EnumType.STRING)
    private ResourceType resourceType = ResourceType.MATERIAL;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

    @ManyToOne
    @JoinColumn(name = "uploaded_by")
    private User uploadedBy;

    private Instant uploadedAt = Instant.now();

    // Resource Type Enum
    public enum ResourceType {
        MATERIAL,
        ASSIGNMENT,
        LECTURE_NOTE,
        VIDEO,
        DOCUMENT,
        OTHER
    }

    // Constructors
    public ResourceFile() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getOriginalFileName() { return originalFileName; }
    public void setOriginalFileName(String originalFileName) { this.originalFileName = originalFileName; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    public ResourceType getResourceType() { return resourceType; }
    public void setResourceType(ResourceType resourceType) { this.resourceType = resourceType; }
    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }
    public User getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(User uploadedBy) { this.uploadedBy = uploadedBy; }
    public Instant getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(Instant uploadedAt) { this.uploadedAt = uploadedAt; }
    public String getFilename() { return fileName; }
    public void setFilename(String filename) { this.fileName = filename; }
    public String getOriginalFilename() { return originalFileName; }
    public void setOriginalFilename(String originalFilename) { this.originalFileName = originalFilename; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public long getSize() { return fileSize != null ? fileSize : 0L; }
    public void setSize(long size) { this.fileSize = size; }
    public void setUploader(User uploader) { this.uploadedBy = uploader; }
}

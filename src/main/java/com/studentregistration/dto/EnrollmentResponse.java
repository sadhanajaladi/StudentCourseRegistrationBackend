package com.studentregistration.dto;



import java.time.LocalDateTime;

import com.studentregistration.entity.EnrollmentStatus;
import com.studentregistration.entity.CourseProgressStatus;

public class EnrollmentResponse {

    private Long id;
    private Long studentId;
    private String studentName;
    private String studentEmail;
    private Long courseId;
    private String courseTitle;
    private String courseCode;
    private EnrollmentStatus status;
    private CourseProgressStatus progressStatus;
    private LocalDateTime enrolledAt;

    public EnrollmentResponse() {
    }

    public EnrollmentResponse(
            Long id,
            Long studentId,
            String studentName,
            String studentEmail,
            Long courseId,
            String courseTitle,
            String courseCode,
            EnrollmentStatus status,
            CourseProgressStatus progressStatus,
            LocalDateTime enrolledAt) {

        this.id = id;
        this.studentId = studentId;
        this.studentName = studentName;
        this.studentEmail = studentEmail;
        this.courseId = courseId;
        this.courseTitle = courseTitle;
        this.courseCode = courseCode;
        this.status = status;
        this.progressStatus = progressStatus;
        this.enrolledAt = enrolledAt;
    }

    public Long getId() { return id; }
    public Long getStudentId() { return studentId; }
    public String getStudentName() { return studentName; }
    public String getStudentEmail() { return studentEmail; }
    public Long getCourseId() { return courseId; }
    public String getCourseTitle() { return courseTitle; }
    public String getCourseCode() { return courseCode; }
    public EnrollmentStatus getStatus() { return status; }
    public CourseProgressStatus getProgressStatus() { return progressStatus; }
    public LocalDateTime getEnrolledAt() { return enrolledAt; }

    public void setId(Long id) { this.id = id; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public void setStudentEmail(String studentEmail) { this.studentEmail = studentEmail; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
    public void setCourseTitle(String courseTitle) { this.courseTitle = courseTitle; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }
    public void setStatus(EnrollmentStatus status) { this.status = status; }
    public void setProgressStatus(CourseProgressStatus progressStatus) {
        this.progressStatus = progressStatus;
    }
    public void setEnrolledAt(LocalDateTime enrolledAt) {
        this.enrolledAt = enrolledAt;
    }
}


package com.studentregistration.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "enrollments",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_student_course",
            columnNames = {"student_id", "course_id"}
        )
    }
)
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EnrollmentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "progress_status", nullable = false)
    private CourseProgressStatus progressStatus;

    @Column(nullable = false)
    private LocalDateTime enrolledAt;

    public Enrollment() {
    }

    @PrePersist
    public void prePersist() {
        if (enrolledAt == null) {
            enrolledAt = LocalDateTime.now();
        }

        if (status == null) {
            status = EnrollmentStatus.ENROLLED;
        }

        if (progressStatus == null) {
            progressStatus = CourseProgressStatus.NOT_STARTED;
        }
    }

    // Getters

    public Long getId() {
        return id;
    }

    public User getStudent() {
        return student;
    }

    public Course getCourse() {
        return course;
    }

    public EnrollmentStatus getStatus() {
        return status;
    }

    public CourseProgressStatus getProgressStatus() {
        return progressStatus;
    }

    public LocalDateTime getEnrolledAt() {
        return enrolledAt;
    }

    // Setters

    public void setId(Long id) {
        this.id = id;
    }

    public void setStudent(User student) {
        this.student = student;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public void setStatus(EnrollmentStatus status) {
        this.status = status;
    }

    public void setProgressStatus(CourseProgressStatus progressStatus) {
        this.progressStatus = progressStatus;
    }

    public void setEnrolledAt(LocalDateTime enrolledAt) {
        this.enrolledAt = enrolledAt;
    }
}

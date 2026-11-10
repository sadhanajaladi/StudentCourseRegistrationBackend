
package com.studentregistration.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.studentregistration.dto.EnrollmentRequest;
import com.studentregistration.dto.EnrollmentResponse;
import com.studentregistration.entity.CourseProgressStatus;
import com.studentregistration.service.EnrollmentService;

@RestController
@RequestMapping
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    // Student enrolls in a course
    @PostMapping("/api/student/enrollments")
    @ResponseStatus(HttpStatus.CREATED)
    public EnrollmentResponse enrollStudent(
            @Valid @RequestBody EnrollmentRequest request,
            Authentication authentication) {

        return enrollmentService.enrollStudent(
                authentication.getName(),
                request.getCourseId());
    }

    // Student views their own enrollments
    @GetMapping("/api/student/enrollments")
    public List<EnrollmentResponse> getMyEnrollments(
            Authentication authentication) {

        return enrollmentService.getStudentEnrollments(
                authentication.getName());
    }

    // Student updates course progress
    @PutMapping("/api/student/enrollments/{enrollmentId}/progress")
    public EnrollmentResponse updateProgress(
            @PathVariable Long enrollmentId,
            @RequestBody ProgressUpdateRequest request,
            Authentication authentication) {

        return enrollmentService.updateProgress(
                authentication.getName(),
                enrollmentId,
                request.getProgressStatus());
    }

    // Admin views all enrollments
    @GetMapping("/api/admin/enrollments")
    public List<EnrollmentResponse> getAllEnrollments() {

        return enrollmentService.getAllEnrollments();
    }

    // Request body for progress update
    public static class ProgressUpdateRequest {

        private CourseProgressStatus progressStatus;

        public CourseProgressStatus getProgressStatus() {
            return progressStatus;
        }

        public void setProgressStatus(CourseProgressStatus progressStatus) {
            this.progressStatus = progressStatus;
        }
    }
}

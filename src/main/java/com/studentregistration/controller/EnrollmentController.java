package com.studentregistration.controller;


import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(EnrollmentController.class);

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
        String studentEmail = authentication.getName();
        log.info("[EnrollmentController] Student {} enrolling in course ID: {}", studentEmail, request.getCourseId());
        EnrollmentResponse response = enrollmentService.enrollStudent(studentEmail, request.getCourseId());
        log.info("[EnrollmentController] Successfully enrolled {} into course ID: {}", studentEmail, request.getCourseId());
        return response;
    }

    // Student views their own enrollments
    @GetMapping("/api/student/enrollments")
    public List<EnrollmentResponse> getMyEnrollments(Authentication authentication) {
        String studentEmail = authentication.getName();
        log.info("[EnrollmentController] Fetching enrollments for student: {}", studentEmail);
        return enrollmentService.getStudentEnrollments(studentEmail);
    }

    // Student updates course progress
    @PutMapping("/api/student/enrollments/{enrollmentId}/progress")
    public EnrollmentResponse updateProgress(
            @PathVariable Long enrollmentId,
            @RequestBody ProgressUpdateRequest request,
            Authentication authentication) {
        String studentEmail = authentication.getName();
        log.info("[EnrollmentController] Student {} updating enrollment {} progress to: {}", studentEmail, enrollmentId, request.getProgressStatus());
        return enrollmentService.updateProgress(studentEmail, enrollmentId, request.getProgressStatus());
    }

    // Admin views all enrollments
    @GetMapping("/api/admin/enrollments")
    public List<EnrollmentResponse> getAllEnrollments() {
        log.info("[EnrollmentController] Admin requested all student enrollments");
        List<EnrollmentResponse> list = enrollmentService.getAllEnrollments();
        log.debug("[EnrollmentController] Total enrollments returned: {}", list.size());
        return list;
    }

    public static class ProgressUpdateRequest {
        private CourseProgressStatus progressStatus;
        public CourseProgressStatus getProgressStatus() { return progressStatus; }
        public void setProgressStatus(CourseProgressStatus progressStatus) { this.progressStatus = progressStatus; }
    }
}

package com.studentregistration.controller;


import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.studentregistration.dto.StudentResponse;
import com.studentregistration.service.AdminService;
import com.studentregistration.service.EnrollmentService;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    private final AdminService adminService;
    private final EnrollmentService enrollmentService;

    public AdminController(AdminService adminService, EnrollmentService enrollmentService) {
        this.adminService = adminService;
        this.enrollmentService = enrollmentService;
    }

    @GetMapping("/students")
    public List<StudentResponse> getAllStudents() {
        log.info("[AdminController] Admin requested list of all registered students");
        List<StudentResponse> students = adminService.getAllStudents();
        log.info("[AdminController] Retrieved {} registered students", students.size());
        return students;
    }

    @GetMapping("/students/{studentId}")
    public StudentResponse getStudentById(@PathVariable Long studentId) {
        log.info("[AdminController] Admin requested details for student ID: {}", studentId);
        return adminService.getStudentById(studentId);
    }

    @DeleteMapping("/enrollments/{enrollmentId}")
    public ResponseEntity<String> deleteEnrollment(@PathVariable Long enrollmentId) {
        log.info("[AdminController] Admin deleting enrollment ID: {}", enrollmentId);
        enrollmentService.deleteEnrollment(enrollmentId);
        log.info("[AdminController] Successfully deleted enrollment ID: {}", enrollmentId);
        return ResponseEntity.ok("Enrollment deleted successfully. Student notification email was attempted.");
    }

    @DeleteMapping("/students/{studentId}")
    public ResponseEntity<String> deleteStudent(@PathVariable Long studentId) {
        log.info("[AdminController] Admin deleting student ID: {}", studentId);
        adminService.deleteStudent(studentId);
        log.info("[AdminController] Successfully deleted student ID: {}", studentId);
        return ResponseEntity.ok("Student account deleted successfully. Notification email was attempted.");
    }
}

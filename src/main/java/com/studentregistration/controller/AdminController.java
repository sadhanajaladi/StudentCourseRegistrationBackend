
package com.studentregistration.controller;

import java.util.List;

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

    private final AdminService adminService;
    private final EnrollmentService enrollmentService;

    public AdminController(
            AdminService adminService,
            EnrollmentService enrollmentService) {

        this.adminService = adminService;
        this.enrollmentService = enrollmentService;
    }

    @GetMapping("/students")
    public List<StudentResponse> getAllStudents() {
        return adminService.getAllStudents();
    }
    @GetMapping("/students/{studentId}")
    public StudentResponse getStudentById(
            @PathVariable Long studentId) {

        return adminService.getStudentById(studentId);
    }
    

@DeleteMapping("/enrollments/{enrollmentId}")
public ResponseEntity<String> deleteEnrollment(
        @PathVariable Long enrollmentId) {

    enrollmentService.deleteEnrollment(enrollmentId);

    return ResponseEntity.ok(
            "Enrollment deleted successfully. Student notification email was attempted.");
}

@DeleteMapping("/students/{studentId}")
public ResponseEntity<String> deleteStudent(
        @PathVariable Long studentId) {

    adminService.deleteStudent(studentId);

    return ResponseEntity.ok(
            "Student account deleted successfully. "
            + "Notification email was attempted.");
}

}

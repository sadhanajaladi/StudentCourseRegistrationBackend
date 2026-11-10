
package com.studentregistration.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.studentregistration.dto.StudentResponse;
import com.studentregistration.entity.Enrollment;
import com.studentregistration.entity.Role;
import com.studentregistration.repository.UserRepository;
import com.studentregistration.entity.User;
import com.studentregistration.repository.EnrollmentRepository;
import com.studentregistration.repository.UserRepository;

@Service
public class AdminService {


private final UserRepository userRepository;
private final EnrollmentRepository enrollmentRepository;
private final EmailService emailService;

public AdminService(
        UserRepository userRepository,
        EnrollmentRepository enrollmentRepository,
        EmailService emailService) {

    this.userRepository = userRepository;
    this.enrollmentRepository = enrollmentRepository;
    this.emailService = emailService;
}


    @Transactional(readOnly = true)
    public List<StudentResponse> getAllStudents() {

        return userRepository.findByRoleOrderByNameAsc(Role.STUDENT)
                .stream()
                .map(student -> new StudentResponse(
                        student.getId(),
                        student.getName(),
                        student.getEmail(),
                        student.getCreatedAt()))
                .toList();
    }
    
    @Transactional(readOnly = true)
    public StudentResponse getStudentById(Long studentId) {

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Student not found with ID: " + studentId));

        if (student.getRole() != Role.STUDENT) {
            throw new IllegalArgumentException(
                    "The requested ID does not belong to a student.");
        }

        return new StudentResponse(
                student.getId(),
                student.getName(),
                student.getEmail(),
                student.getCreatedAt());
    }

@Transactional
public void deleteStudent(Long studentId) {

    User student = userRepository.findById(studentId)
            .orElseThrow(() ->
                    new IllegalArgumentException(
                            "Student not found with ID: " + studentId));

    if (student.getRole() != Role.STUDENT) {
        throw new IllegalArgumentException(
                "This operation can only delete student accounts.");
    }

    String studentEmail = student.getEmail();
    String studentName = student.getName();

    // Delete this student's enrollments first.
    List<Enrollment> enrollments =
            enrollmentRepository.findByStudent_IdOrderByEnrolledAtDesc(
                    studentId);

    enrollmentRepository.deleteAll(enrollments);

    // Delete the student account.
    userRepository.delete(student);

    // Send notification to the student's registered email.
    emailService.sendAccountDeletionEmail(studentEmail, studentName);
}

}

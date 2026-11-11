package com.studentregistration.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.studentregistration.dto.EnrollmentResponse;
import com.studentregistration.entity.Course;
import com.studentregistration.entity.CourseProgressStatus;
import com.studentregistration.entity.Enrollment;
import com.studentregistration.entity.EnrollmentStatus;
import com.studentregistration.entity.Role;
import com.studentregistration.entity.User;
import com.studentregistration.repository.CourseRepository;
import com.studentregistration.repository.EnrollmentRepository;
import com.studentregistration.repository.UserRepository;

@Service
public class EnrollmentService {

    private static final Logger log = LoggerFactory.getLogger(EnrollmentService.class);

    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final EmailService emailService;

    public EnrollmentService(
            EnrollmentRepository enrollmentRepository,
            UserRepository userRepository,
            CourseRepository courseRepository,
            EmailService emailService) {

        this.enrollmentRepository = enrollmentRepository;
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.emailService = emailService;
    }

    // Student enrolls in a course
    @Transactional
    public EnrollmentResponse enrollStudent(
            String studentEmail, Long courseId) {

        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Student account not found."));

        if (student.getRole() != Role.STUDENT) {
            throw new IllegalArgumentException(
                    "Only students can enroll in courses.");
        }

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Course not found with ID: " + courseId));

        if (enrollmentRepository.existsByStudent_IdAndCourse_Id(
                student.getId(), course.getId())) {
            throw new IllegalArgumentException(
                    "You are already enrolled in this course.");
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setCourse(course);
        enrollment.setStatus(EnrollmentStatus.ENROLLED);
        enrollment.setProgressStatus(CourseProgressStatus.NOT_STARTED);

        Enrollment saved = enrollmentRepository.save(enrollment);

        emailService.sendEnrollmentConfirmation(
                student.getEmail(),
                student.getName(),
                course.getTitle(),
                course.getCourseCode());

        return convertToResponse(saved);
    }

    // Student views their own enrollments
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getStudentEnrollments(
            String studentEmail) {

        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Student account not found."));

        return enrollmentRepository
                .findByStudent_IdOrderByEnrolledAtDesc(student.getId())
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    // Admin views all enrollments
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getAllEnrollments() {

        return enrollmentRepository
                .findAllByOrderByEnrolledAtDesc()
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    // Student updates their own course progress
    @Transactional
    public EnrollmentResponse updateProgress(
            String studentEmail,
            Long enrollmentId,
            CourseProgressStatus progressStatus) {

        if (progressStatus == null) {
            throw new IllegalArgumentException(
                    "Progress status is required.");
        }

        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Student account not found."));

        if (student.getRole() != Role.STUDENT) {
            throw new IllegalArgumentException(
                    "Only students can update course progress.");
        }

        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Enrollment not found."));

        if (!enrollment.getStudent().getId().equals(student.getId())) {
            throw new IllegalArgumentException(
                    "You cannot update another student's enrollment.");
        }

        enrollment.setProgressStatus(progressStatus);

        Enrollment updated = enrollmentRepository.save(enrollment);

        return convertToResponse(updated);
    }

    // Convert entity into response DTO
    private EnrollmentResponse convertToResponse(Enrollment enrollment) {

        User student = enrollment.getStudent();
        Course course = enrollment.getCourse();

        return new EnrollmentResponse(
                enrollment.getId(),
                student.getId(),
                student.getName(),
                student.getEmail(),
                course.getId(),
                course.getTitle(),
                course.getCourseCode(),
                enrollment.getStatus(),
                enrollment.getProgressStatus(),
                enrollment.getEnrolledAt());
    }

    // Admin deletes an enrollment
    @Transactional
    public void deleteEnrollment(Long enrollmentId) {

        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Enrollment not found with ID: " + enrollmentId));

        String studentEmail = enrollment.getStudent().getEmail();
        String studentName = enrollment.getStudent().getName();
        String courseTitle = enrollment.getCourse().getTitle();
        String courseCode = enrollment.getCourse().getCourseCode();

        enrollmentRepository.delete(enrollment);

        emailService.sendEnrollmentDeletionEmail(
                studentEmail,
                studentName,
                courseTitle,
                courseCode);
    }
}

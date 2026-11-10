
package com.studentregistration.service;

import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.studentregistration.entity.Course;
import com.studentregistration.repository.CourseRepository;

@Service
public class CourseService {

    private final CourseRepository courseRepository;

    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    @Transactional
    public Course addCourse(Course course) {

        String code = course.getCourseCode()
                .trim()
                .toUpperCase(Locale.ROOT);

        if (courseRepository.existsByCourseCode(code)) {
            throw new IllegalArgumentException(
                    "A course with this code already exists.");
        }

        course.setCourseCode(code);
        course.setTitle(course.getTitle().trim());

        return courseRepository.save(course);
    }

    @Transactional(readOnly = true)
    public List<Course> getAllCourses() {
        return courseRepository.findAllByOrderByTitleAsc();
    }

    @Transactional(readOnly = true)
    public Course getCourseById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Course not found with ID: " + id));
    }
}

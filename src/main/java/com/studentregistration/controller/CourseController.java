package com.studentregistration.controller;


import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.studentregistration.entity.Course;
import com.studentregistration.service.CourseService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private static final Logger log = LoggerFactory.getLogger(CourseController.class);

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    public List<Course> getAllCourses() {
        log.info("[CourseController] Fetching all available courses");
        List<Course> courses = courseService.getAllCourses();
        log.debug("[CourseController] Total courses returned: {}", courses.size());
        return courses;
    }

    @GetMapping("/{id}")
    public Course getCourseById(@PathVariable Long id) {
        log.info("[CourseController] Fetching course with ID: {}", id);
        return courseService.getCourseById(id);
    }

    @PostMapping
    public ResponseEntity<Course> createCourse(@Valid @RequestBody Course course) {
        log.info("[CourseController] Creating new course: [{}] {}", course.getCourseCode(), course.getTitle());
        Course created = courseService.addCourse(course);
        log.info("[CourseController] Created course with ID: {}", created.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}

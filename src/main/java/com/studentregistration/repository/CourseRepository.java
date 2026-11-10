
package com.studentregistration.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.studentregistration.entity.Course;

public interface CourseRepository extends JpaRepository<Course, Long> {

    boolean existsByCourseCode(String courseCode);

    List<Course> findAllByOrderByTitleAsc();
}

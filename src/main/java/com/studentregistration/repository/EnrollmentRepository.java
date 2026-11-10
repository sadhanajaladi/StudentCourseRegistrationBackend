
package com.studentregistration.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.studentregistration.entity.Enrollment;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EnrollmentRepository
        extends JpaRepository<Enrollment, Long> {

    boolean existsByStudent_IdAndCourse_Id(
            Long studentId, Long courseId);

    List<Enrollment> findByStudent_IdOrderByEnrolledAtDesc(
            Long studentId);

    List<Enrollment> findAllByOrderByEnrolledAtDesc();
    
    @Query("""
    	    SELECT e FROM Enrollment e
    	    JOIN FETCH e.student
    	    JOIN FETCH e.course
    	    ORDER BY e.enrolledAt DESC
    	    """)
    	List<Enrollment> findAllWithStudentAndCourse();
}

package com.example.FeedbackSystem.repository;

import com.example.FeedbackSystem.model.Course;
import com.example.FeedbackSystem.model.Enrollment;
import com.example.FeedbackSystem.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Integer> {

    boolean existsByCourseAndStudent(Course course, User student);

    List<Enrollment> findByStudent(User student);

    Enrollment findEnrollmentByStudentUserIdAndCourseCourseId(int studentId, int courseId);

    List<Enrollment> findByStudent_RollNo(String rollNo);

    @Query("SELECT COUNT(e) " +
            "FROM Enrollment e " +
            "LEFT JOIN e.course c " +
            "WHERE c.courseId = :courseId ")
    Integer getCourseEnrollmentCount(int courseId);

    @Query("SELECT COUNT(e) " +
            "FROM Enrollment e " +
            "LEFT JOIN e.student s " +
            "WHERE s.userId = :studentId ")
    Integer getStudentEnrollmentCount(int studentId);

    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.student.userId = :userId")
    Integer countStudentTotalEnrollments(@Param("userId") int userId);
}

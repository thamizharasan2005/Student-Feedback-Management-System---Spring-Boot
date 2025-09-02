package com.example.FeedbackSystem.repository;

import com.example.FeedbackSystem.DTO.FeedbacksByInstructor;
import com.example.FeedbackSystem.model.Instructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface InstructorRepository extends JpaRepository<Instructor, Integer> {

    @Query(value = "SELECT i.instructorId AS instructorId, " +
            "i.instructorName AS instructorName, " +
            "c.courseName AS courseName, " +
            "f AS feedbacksByCourse " +
            "FROM Instructor i " +
            "LEFT JOIN i.courses c " +
            "LEFT JOIN c.feedbacks f " )
    List<FeedbacksByInstructor> getAllFeedbacksByInstructor();

    @Query("SELECT i FROM Instructor i LEFT JOIN i.courses c WHERE c IS NULL")
    List<Instructor> findUnassignedInstructors();
}

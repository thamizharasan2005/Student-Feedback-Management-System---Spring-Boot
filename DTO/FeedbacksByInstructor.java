package com.example.FeedbackSystem.DTO;

import java.util.List;

public interface FeedbacksByInstructor {
    Integer getInstructorId();
    String getInstructorName();
    String getCourseName();
    List<FeedbackProjection> getFeedbacksByCourse();
}

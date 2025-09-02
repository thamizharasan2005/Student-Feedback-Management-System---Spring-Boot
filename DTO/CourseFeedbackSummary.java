package com.example.FeedbackSystem.DTO;


public interface CourseFeedbackSummary {
    int getCourseId();
    String getCourseName();
    String getInstructorName();
    Double getAverageRating();
    long getFeedbackCount();
}

package com.example.FeedbackSystem.DTO;

import java.util.Date;

public interface FeedbackProjection {
    int getFeedbackId();
    int getCourseRating();
    int getInstructorRating();
    String getCourseComment();
    String getInstructorComment();
    Date getSubmittedAt();
}

package com.example.FeedbackSystem.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FeedbackResponseDTO {

    private int feedbackId;
    private int courseRating;
    private int instructorRating;
    private String courseComment;
    private String instructorComment;
    private boolean anonymous;
    private String studentName;
    private String courseName;
    private String instructorName;
    private LocalDate submittedAt;
}

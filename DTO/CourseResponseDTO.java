package com.example.FeedbackSystem.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseResponseDTO {
    private int courseId;
    private String courseName;
    private String courseDescription;
    private String instructorName;
}

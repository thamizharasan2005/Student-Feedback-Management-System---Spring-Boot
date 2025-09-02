package com.example.FeedbackSystem.DTO;


import com.example.FeedbackSystem.model.Instructor;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CourseDTO {
    @NotBlank(message = "Course name is required")
    private String courseName;
    @NotBlank(message = "Course description is required")
    private String courseDescription;

    private Instructor instructor;
}

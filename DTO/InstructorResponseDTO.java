package com.example.FeedbackSystem.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InstructorResponseDTO {
    int instructorId;
    String instructorName;
    List<CourseResponseDTO> assignedCourses;
}

package com.example.FeedbackSystem.model;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDate;

@Entity
@Data
public class Enrollment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int enrollId;

    @ManyToOne
    private User student;

    @ManyToOne
    private Course course;

    @CreatedDate
    private LocalDate enrollmentDate;
}

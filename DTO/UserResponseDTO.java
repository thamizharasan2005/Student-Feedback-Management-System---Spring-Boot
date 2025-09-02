package com.example.FeedbackSystem.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class UserResponseDTO {
    private int userId;
    private String username;
    private String email;
    private String rollNo;

}

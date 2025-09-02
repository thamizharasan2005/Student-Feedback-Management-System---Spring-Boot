package com.example.FeedbackSystem.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class JwtResponse {
    String accessToken;
    String RefreshToken;
}

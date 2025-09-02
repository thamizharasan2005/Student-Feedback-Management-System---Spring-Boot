package com.example.FeedbackSystem.DTO;

import lombok.Data;

@Data
public class JwtRefreshToken {
    private String refreshToken;
}


//Spring will automatically map the incoming JSON body to this class using Jackson
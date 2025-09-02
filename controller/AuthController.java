package com.example.FeedbackSystem.controller;

import com.example.FeedbackSystem.DTO.JwtRefreshToken;
import com.example.FeedbackSystem.DTO.JwtResponse;
import com.example.FeedbackSystem.DTO.UserDTO;
import com.example.FeedbackSystem.Exception.ResourceNotFoundException;
import com.example.FeedbackSystem.model.RefreshToken;
import com.example.FeedbackSystem.model.User;
import com.example.FeedbackSystem.repository.UserRepository;
import com.example.FeedbackSystem.security.CustomUserDetailsService;
import com.example.FeedbackSystem.security.JwtUtils;
import com.example.FeedbackSystem.service.EmailService;
import com.example.FeedbackSystem.service.RefreshTokenService;
import com.example.FeedbackSystem.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    UserService userService;
    @Autowired
    UserRepository userRepo;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    CustomUserDetailsService userDetailsService;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    RefreshTokenService refreshTokenService;

    @Autowired
    EmailService emailService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserDTO user){
    User savedUser = userService.addUser(user);

    //sends an email to the registered user from the email in the application.properties
        String htmlContent = "<h1 style=\"color: #4CAF50;\">Welcome, " + savedUser.getUsername() + "</h1>"
                + "<p>Your registration was <b>successful</b> 🎉</p>"
                + "<p>Thanks for joining <i>Feedback System</i>.</p>"
                + "<hr>"
                + "<small>This is an automated email, please do not reply.</small>";
        emailService.simpleMailSender(
                savedUser.getEmail(),
                "Registered Successfully!",
                htmlContent
        );
        return ResponseEntity.ok(savedUser);
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody UserDTO user){
        String email = user.getEmail();
        String password = user.getPassword();
        try{
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    email,
                    password
            ));
        }
        catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        // generate access token
        String jwtToken = jwtUtils.generateToken(userDetails);

        // generate refresh token
        User dbUser = userRepo.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("Invalid email"));
        RefreshToken refreshToken = refreshTokenService.issue(dbUser);

        return ResponseEntity.ok(new JwtResponse(jwtToken, refreshToken.getToken()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody JwtRefreshToken refreshToken){
        String presented = refreshToken.getRefreshToken();
        RefreshToken current = refreshTokenService.verifyUsable(presented);
        User user = current.getUser();

        // Rotate old refresh token with a new one
        RefreshToken nextRefreshToken = refreshTokenService.rotate(current);

        // Generate new access token
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String newAccess = jwtUtils.generateToken(userDetails);

        return ResponseEntity.ok(new JwtResponse(newAccess, nextRefreshToken.getToken()));
    }
}

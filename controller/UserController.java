package com.example.FeedbackSystem.controller;


import com.example.FeedbackSystem.DTO.UserDTO;
import com.example.FeedbackSystem.DTO.UserResponseDTO;
import com.example.FeedbackSystem.model.User;
import com.example.FeedbackSystem.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    UserService userService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/all")
    public ResponseEntity<List<User>> getAllUsers(){
        return new ResponseEntity<>(userService.getAllUsers(), HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    @GetMapping("/all-by")
    public ResponseEntity<List<UserResponseDTO>> getAllUsersByRole(
            @RequestParam(defaultValue = "STUDENT") String role) {
        User.Role roleEnum = User.Role.valueOf(role.toUpperCase());
        return new ResponseEntity<>(userService.getAllUsersByRole(roleEnum), HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    @GetMapping("/id/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable int userId){
        return new ResponseEntity<>(userService.getUserById(userId), HttpStatus.OK);
    }

    @PreAuthorize("permitAll()")
    @PostMapping("/create")
    public ResponseEntity<?> addUser(@Valid @RequestBody UserDTO userDTO){
        try {
            return new ResponseEntity<>(userService.addUser(userDTO), HttpStatus.OK);
        }
        catch (Exception e){
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PreAuthorize("permitAll()")
    @PutMapping("/update/{userId}")
    public ResponseEntity<?> updateUser(@PathVariable int userId, @Valid @RequestBody UserDTO userDTO){
        try {
            return new ResponseEntity<>(userService.updateUser(userId, userDTO), HttpStatus.OK);
        }
        catch (Exception e){
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/delete/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable int userId){
        try{
            userService.deleteByUserId(userId);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    @GetMapping("/searchBy")
    public ResponseEntity<?> findByRollNo(@RequestParam String rollNo){
        try {
            return ResponseEntity.ok(userService.getByRollNo(rollNo));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    @GetMapping("/by-email")
    public ResponseEntity<?> findByEmail(@RequestParam String email){
        try {
            return ResponseEntity.ok(userService.getByEmail(email));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}

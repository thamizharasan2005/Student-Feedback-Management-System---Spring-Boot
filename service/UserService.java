package com.example.FeedbackSystem.service;

import com.example.FeedbackSystem.DTO.analytics.TopRatedStudentsDTO;
import com.example.FeedbackSystem.DTO.UserDTO;
import com.example.FeedbackSystem.DTO.UserResponseDTO;
import com.example.FeedbackSystem.Exception.BadRequestException;
import com.example.FeedbackSystem.Exception.ResourceNotFoundException;
import com.example.FeedbackSystem.model.User;
import com.example.FeedbackSystem.repository.UserRepository;
import com.example.FeedbackSystem.specification.UserSpecification;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    UserRepository userRepo;

    @Autowired
    PasswordEncoder passwordEncoder;

    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

    public User getUserById(int userId) {
        return userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found!"));
    }

    // Return all Users as DTO
    public List<UserResponseDTO> getAllUsersAsDTO(){
        return userRepo.findAll().stream()
                .map(this::convertToUserResponseDTO)
                .toList();
    }
    // Return all users by userId -- fetch all required data in one query - faster
    public List<User> getAllUsersById(){
        return userRepo.findAllById(getAllUsersAsDTO().stream().
                map(UserResponseDTO::getUserId)
                .toList());
    }

    public User convertUserDTOToUser(User user, @Valid UserDTO userDTO){
        user.setUsername(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setRole(userDTO.getRole());
        if(userDTO.getRole().equals(User.Role.ADMIN)){
            user.setRollNo(userDTO.getAdminId());
        }
        if(userDTO.getRole().equals(User.Role.STUDENT)) {
            user.setRollNo(userDTO.getRollNo());
        }

        return user;
    }

    public UserResponseDTO convertToUserResponseDTO(User user){
        return new UserResponseDTO(
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getRollNo()
        );
    }

    public User addUser(UserDTO userDTO) {
        Optional<User> isExist = userRepo.findByEmail(userDTO.getEmail());

        if(isExist.isPresent()){
            throw new BadRequestException("User already exists.");
        }

        if(userDTO.getRole() == User.Role.STUDENT){
            System.out.println(userDTO.getRollNo());
            if(userDTO.getRollNo() == null)
                throw new BadRequestException("Invalid roll number");
        }

        if(userDTO.getRole().equals(User.Role.ADMIN)){
            if(userDTO.getAdminId() == null){
                throw new BadRequestException("Admin id is required");
            }
        }

        User user = new User();
        return userRepo.save(convertUserDTOToUser(user, userDTO));
    }


    public User updateUser(int userId, UserDTO userDTO) {
        if(!userRepo.existsById(userId)){
            throw new ResourceNotFoundException("User not found! Id: "+userId);
        }

        if(userRepo.existsByEmailAndUserIdNot(userDTO.getEmail(), userId)) {
            throw new BadRequestException("Email already exists.");
        }

        if(userDTO.getRole().equals(User.Role.ADMIN)){
            if(userDTO.getAdminId() == null){
                throw new BadRequestException("Admin id is required");
            }
        }
        User userExist = getUserById(userId);

        return userRepo.save(convertUserDTOToUser(userExist, userDTO));
    }

    public void deleteByUserId(int userId) {
        if(!userRepo.existsById(userId)){
            throw new ResourceNotFoundException("User not exist - ID:"+userId);
        }

        userRepo.deleteById(userId);
    }

    public User getByRollNo(String rollNo){
        return userRepo.findByRollNo(rollNo)
                .orElseThrow(() ->new ResourceNotFoundException("Student with rollNo "+rollNo+" not found."));
    }

    public List<UserResponseDTO> getAllUsersByRole(User.Role roleEnum){
        return userRepo.findByRole(roleEnum).stream()
                .map(user -> new UserResponseDTO(user.getUserId(), user.getUsername(), user.getEmail(), user.getRollNo()))
                .collect(Collectors.toList());
    }

    public UserResponseDTO getByEmail(String email){
        User student = userRepo.findByEmail(email)
                .orElseThrow(() ->new ResourceNotFoundException("Student with email "+email+" not found."));
        return convertToUserResponseDTO(student);
    }

    public Integer getTotalStudentsCount() {
        return userRepo.totalStudentsCount();
    }

    public List<TopRatedStudentsDTO> getTopRatedStudents(int pageNumber, int limit) {
        Pageable pageable = PageRequest.of(pageNumber, limit);
        return userRepo.findTopStudentsByFeedbacks(pageable);
    }

    public List<UserResponseDTO> searchUser(Integer userId,
                                            Integer studentId,
                                            String studentName,
                                            String rollNo
    ){
        Specification<User> specification = Specification.allOf(
                UserSpecification.hasUserId(userId),
                UserSpecification.studentById(studentId),
                UserSpecification.hasStudentName(studentName),
                UserSpecification.hasRollNo(rollNo)
        );
        return userRepo.findAll(specification).stream()
                .map(this::convertToUserResponseDTO)
                .toList();
    }

    // ADMIN search specifications
    public List<UserResponseDTO> searchAdmin(Integer adminUId,
                                            String adminId) {
        Specification<User> specification = Specification.allOf(
                UserSpecification.adminById(adminUId),
                UserSpecification.hasAdminIdNo(adminId)
        );
        return userRepo.findAll(specification).stream()
                .map(this::convertToUserResponseDTO)
                .toList();
    }

    public List<UserResponseDTO> findStudentsWithoutFeedback(){
        Specification<User> specification = Specification.allOf(
                UserSpecification.studentsWithoutFeedback()
        );
        return userRepo.findAll(specification).stream()
                .map(this::convertToUserResponseDTO)
                .toList();
    }

    public List<UserResponseDTO> findStudentsWithoutEnrollment(){
        Specification<User> specification = Specification.allOf(
                UserSpecification.studentsWithoutEnrollments()
        );
        return userRepo.findAll(specification).stream()
                .map(this::convertToUserResponseDTO)
                .toList();
    }

    public List<UserResponseDTO> studentsEnrolledToThisCourse(Integer courseId){
        Specification<User> specification = Specification.allOf(
                UserSpecification.studentsEnrolledToThisCourse(courseId)
        );
        return userRepo.findAll(specification).stream()
                .map(this::convertToUserResponseDTO)
                .toList();
    }
}

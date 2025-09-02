package com.example.FeedbackSystem.repository;

import com.example.FeedbackSystem.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.rollNo = :rollNo")
    Optional<User> findByRollNo(String rollNo);

    // <> exclude the userId that passed, checks email with remaining users while updating
    boolean existsByEmailAndUserIdNot(String email, int userId);

    @Query("SELECT u FROM User u WHERE u.role = :role")
    List<User> findByRole(@Param("role") User.Role role);
}

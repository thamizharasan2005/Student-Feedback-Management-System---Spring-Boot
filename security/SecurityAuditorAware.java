package com.example.FeedbackSystem.security;

import com.example.FeedbackSystem.model.User;
import com.example.FeedbackSystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public class SecurityAuditorAware implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication == null && !authentication.isAuthenticated()){
            return Optional.empty();
        }

        return Optional.of(authentication.getName());
    }
}

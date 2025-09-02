package com.example.FeedbackSystem.configure;

import com.example.FeedbackSystem.repository.UserRepository;
import com.example.FeedbackSystem.security.SecurityAuditorAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaAuditingConfig {

    @Bean
    public AuditorAware<String> auditorProvider(){
        return new SecurityAuditorAware();
    }
}

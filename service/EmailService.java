package com.example.FeedbackSystem.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class EmailService {

    @Autowired
    JavaMailSender mailSender;

    public void simpleMailSender(String to, String subject, String htmlBody){
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true); // to do multipart mail (img, aud)
            // getting file using url path
            String url = System.getProperty("user.dir") + "\\sukuna.jpeg";
            FileSystemResource file = new FileSystemResource(new File(url));

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);  // the true tells that "this is HTML file text". if not it takes the string as normal
            helper.addAttachment(file.getFilename(), file); // file is attached to the mail
            mailSender.send(message);
        } catch (MessagingException me) {
            throw new RuntimeException(me.getMessage());
        }
    }

    // Use thymeleaf template dependency for beautiful emails
}

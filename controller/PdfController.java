package com.example.FeedbackSystem.controller;

import com.example.FeedbackSystem.DTO.UserResponseDTO;
import com.example.FeedbackSystem.model.User;
import com.example.FeedbackSystem.service.PdfReportService;
import com.example.FeedbackSystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/report")
public class PdfController {

    @Autowired
    PdfReportService pdfService;



    @GetMapping("/user")
    public String generateUserReport(){


        String filePath = pdfService.generateUserReport();
        return "Pdf generated in: "+filePath;
    }
}

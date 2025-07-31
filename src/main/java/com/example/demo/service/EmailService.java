package com.example.demo.service;

//EmailService.java

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

 @Async
 public void sendVerificationEmail(String to) {
     // In real app: use JavaMailSender to send actual email
     System.out.println("Verification email sent to: " + to);
 }
}
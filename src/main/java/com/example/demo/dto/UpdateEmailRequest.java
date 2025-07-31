package com.example.demo.dto;

//UpdateEmailRequest.java

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateEmailRequest {
 @NotBlank
 @Email
 private String newEmail;
}
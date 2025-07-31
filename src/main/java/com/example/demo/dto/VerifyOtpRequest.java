package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;

//src/main/java/com/example/demo/dto/VerifyOtpRequest.java

import lombok.Data;

@Data
public class VerifyOtpRequest {
 @NotBlank
 private String email;

 @NotBlank
 private String otp;
}
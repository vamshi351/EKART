package com.example.demo.dto;

//UpdateUserRequest.java

import lombok.Data;

@Data
public class UpdateUserRequest {
 private String name;
 private String role; // ADMIN or USER
}
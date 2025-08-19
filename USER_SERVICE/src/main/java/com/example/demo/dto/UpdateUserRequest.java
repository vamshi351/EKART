package com.example.demo.dto;

//UpdateUserRequest.java

import lombok.Data;

@Data
public class UpdateUserRequest {
    private String name;
    private String phone;
    private String role; // optional, only for admin
}

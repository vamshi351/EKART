package com.example.demo.dto;

import com.example.demo.model.User;

//src/main/java/com/example/demo/dto/UserDTO.java

public class UserDTO {
    private String name;

    public UserDTO(User user) {
        this.name = user.getName();
    }

    // ⚠️ Add only this safely
    public String getName() {
        return name;
    }

    // ❌ Don't add `User user` as a field or @ToString/@Data
}

package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

//src/main/java/com/example/demo/dto/UserDTO.java

@Data
@AllArgsConstructor
public class UserDTO {
    private String name;
    private String phone;
    private String email;

    // ❌ Don't add `User user` as a field or @ToString/@Data
}

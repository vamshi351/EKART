package com.example.demo.service;

import java.security.SecureRandom;

public class JwtKeyGenerator {
    public static void mainx(String[] args) {
        SecureRandom secureRandom = new SecureRandom();
        byte[] key = new byte[32]; // 256 bits
        secureRandom.nextBytes(key);
        StringBuilder hexKey = new StringBuilder();
        for (byte b : key) {
            hexKey.append(String.format("%02x", b));
        }
        System.out.println("Your JWT Secret Key: " + hexKey.toString());
    }
}

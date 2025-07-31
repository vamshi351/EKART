package com.example.demo.exception;

//GlobalExceptionHandler.java

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.userservice.exception.UserNotFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

 @ExceptionHandler(UserNotFoundException.class)
 public ResponseEntity<String> handleUserNotFound(UserNotFoundException ex) {
     return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
 }

 @ExceptionHandler(EmailAlreadyExistsException.class)
 public ResponseEntity<String> handleEmailExists(EmailAlreadyExistsException ex) {
     return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
 }

 @ExceptionHandler(Exception.class)
 public ResponseEntity<String> handleGeneral(Exception ex) {
     return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + ex.getMessage());
 }
}
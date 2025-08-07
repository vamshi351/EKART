package com.example.demo.repository;

import java.util.Optional;

//UserRepository.java

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
 Optional<User> findByEmail(String email);
 Optional<User> findByPhone(String phone);
 Optional<User> findByPendingEmail(String pendingEmail);
 


}
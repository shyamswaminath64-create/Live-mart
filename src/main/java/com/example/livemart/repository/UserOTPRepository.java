package com.example.livemart.repository;

import com.example.livemart.model.User;
import com.example.livemart.model.UserOTP;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserOTPRepository extends JpaRepository <UserOTP, Integer> {
    Optional<UserOTP> findByUser(User user);
}

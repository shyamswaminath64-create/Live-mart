package com.example.livemart.service;

import com.example.livemart.model.User;
import com.example.livemart.model.UserOTP;
import com.example.livemart.repository.UserOTPRepository;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.security.SecureRandom;
import java.util.Optional;

@Service
public class OTPService {
    private static final SecureRandom secureRandom = new SecureRandom();
    private final UserOTPRepository userOTPRepository;

    public OTPService(UserOTPRepository userOTPRepository){
        this.userOTPRepository = userOTPRepository;
    }

    public UserOTP generateOTP(User user){
        userOTPRepository.findByUser(user).ifPresent(userOTPRepository::delete);
        int otp = 100000 + secureRandom.nextInt(900000);
        UserOTP userOTP = new UserOTP();
        userOTP.setOtp(otp);
        userOTP.setUser(user);

        return userOTP;
    }

    public int getOTP(User user){
        Optional<UserOTP> usr = userOTPRepository.findByUser(user);
        return usr.map(UserOTP::getOtp).orElse(-1);
    }
}

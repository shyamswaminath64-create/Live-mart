package com.example.livemart.controller;

import com.example.livemart.common.exceptions.UserNotFoundException;
import com.example.livemart.dto.AuthRequest;
import com.example.livemart.model.creators.ProfileCreator;
import com.example.livemart.model.creators.UserCreate;
import com.example.livemart.dto.UserDTO;
import com.example.livemart.model.UserOTP;
import com.example.livemart.model.enums.AuthorizedBy;
import com.example.livemart.model.User;
import com.example.livemart.model.enums.Role;
import com.example.livemart.security.JwtService;
import com.example.livemart.service.EmailSenderService;
import com.example.livemart.service.OTPService;
import com.example.livemart.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping(path = "/auth")
public class AuthController {
    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final OTPService otpGenService;
    private final EmailSenderService emailSenderService;

    public AuthController(UserService userService, JwtService jwtService, AuthenticationManager authenticationManager, OTPService otpGenService, EmailSenderService emailSenderService){
        this.userService = userService;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.otpGenService = otpGenService;
        this.emailSenderService = emailSenderService;
    }

    @PostMapping(path = "/generateToken")
    public ResponseEntity<?> login(@RequestBody AuthRequest authRequest){
        User userInDB;
        try {
            userInDB = userService.findUserByEmail(authRequest.getUsername());
        }
        catch(UserNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Wrong username/password");
        }

        if (userInDB.getAuthorizedBy() == AuthorizedBy.GOOGLE) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login via google");
        }

        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
            );

            if (auth.isAuthenticated()) {
                String token = jwtService.generateToken(authRequest.getUsername());
                return ResponseEntity.ok(Map.of("token", token));
            }
        } catch (AuthenticationException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Wrong username/password123");
        }

        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Wrong username/password");
    }

    @PostMapping(path = "/register")
    public ResponseEntity<UserDTO> register(@RequestBody UserCreate user) throws UserNotFoundException {
        if (user == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid request for user creation");

        if (userService.userExists(user.getEmail()) && userService.findUserByEmail(user.getEmail()).getAuthorized() == true) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User already exists");
        }

        // Validate and save to DB
        var validation = userService.validateUserCreate(user);
        if (!validation.getValue0())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, validation.getValue1());

        user.setAuthorizedBy(AuthorizedBy.LOGIN_FORM); // DO NOT ALLOW USER TO SET
        User userToCreate = (UserCreate.mapToUser(user));

        boolean exists = false;
        try{
            User existingUnverifiedUser = userService.findUserByEmail(user.getEmail());
            exists = true;
            emailSenderService.sendEmail(user.getEmail(), "lively4mart@gmail.com", "Another", "OTP: " + otpGenService.getOTP(existingUnverifiedUser));

            return new ResponseEntity<>(UserDTO.mapUserToDTO(UserCreate.mapToUser(user)), HttpStatus.PARTIAL_CONTENT);
        }
        catch (UserNotFoundException ignored){
        }
        UserOTP otpObject = otpGenService.generateOTP(userToCreate);
        emailSenderService.sendEmail(user.getEmail(), "lively4mart@gmail.com", "Fard", "OTP: " + otpObject.getOtp());

        userToCreate.setOtpObject(otpObject);

        User userResp = userService.createUser(userToCreate);
        if (userResp == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad address!");

        return new ResponseEntity<>(UserDTO.mapUserToDTO(UserCreate.mapToUser(user)), HttpStatus.CREATED);
    }

    @Operation(security = {@SecurityRequirement(name = "bearer-jwt"), @SecurityRequirement(name = "oauth2")})
    @PostMapping(path = "/verifyOTP")
    public ResponseEntity<String> verifyOTP(@AuthenticationPrincipal UserDetails userDetails, @RequestParam int otp){
        if (userDetails == null)
            return new ResponseEntity<>("get out", HttpStatus.UNAUTHORIZED);

        User user;
        try {
            user = userService.findUserByEmail(userDetails.getUsername());
        }
        catch (UserNotFoundException e){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "couldnt find");
        }

        if (user.getAuthorized() == true)
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not allowed after validity");

        int correctOTP = otpGenService.getOTP(user);

        if (correctOTP != -1 && correctOTP == otp){
            user.setAuthorized(true);
            user.setOtpObject(null);
            userService.updateUser(user); // update

            return new ResponseEntity<>("User verified!", HttpStatus.OK);
        }
        return new ResponseEntity<>("Incorrect otp!", HttpStatus.UNAUTHORIZED);
    }

    @PostMapping("/complete-profile")
    public ResponseEntity<UserDTO> completeProfile(@AuthenticationPrincipal OAuth2User oAuth2User, @RequestBody ProfileCreator profileCompletionEntity){
        if (oAuth2User == null)
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not allowed at this moment");

        if (userService.userExists(oAuth2User.getAttribute("email")))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User already exists, there is no need to create a profile");

        User user = new User();
        user.setEmail(oAuth2User.getAttribute("email"));
        user.setName(oAuth2User.getAttribute("name"));
        user.setAuthorized(true);

        if (!userService.validateGoogleProfile(profileCompletionEntity))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Illegal profile completion entity");

        user.setRole(Role.valueOf(profileCompletionEntity.getRole()));
        user.setAddress(profileCompletionEntity.getAddress());
        user.setPhoneNumber(profileCompletionEntity.getPhoneNumber());
        user.setStoreName(profileCompletionEntity.getStoreName());

        user.setAuthorizedBy(AuthorizedBy.GOOGLE);

        User createdUser = userService.createUser(user);

        if (createdUser == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad address!");


        return new ResponseEntity<>(UserDTO.mapUserToDTO(createdUser), HttpStatus.CREATED);
    }
}

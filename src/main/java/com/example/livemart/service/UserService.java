package com.example.livemart.service;

import com.example.livemart.common.exceptions.UserNotFoundException;
import com.example.livemart.model.creators.ProfileCreator;
import com.example.livemart.model.creators.UserCreate;
import com.example.livemart.model.enums.Role;
import com.example.livemart.model.User;
import com.example.livemart.repository.UserRepository;
import org.javatuples.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final GeocodioService geocodioService;
    private final PasswordEncoder encoder;
    private final OTPService otpGenService;
    private final EmailSenderService emailSenderService;

    public Pair<Boolean, String> validateAccess(User user, Role toBlock){
        if (user == null || user.getAuthorized() == false || user.getRole() == toBlock) {
            if (user != null && !user.getAuthorized())
                return new Pair<>(false, "Verify passkey to obtain authentication!");
            if (user != null && user.getRole() == toBlock)
                return new Pair<>(false, toBlock + "cannot access this page!");
            return new Pair<>(false, "User not authenticated");
        }
        return new Pair<>(true, "");
    }

    public boolean validateGoogleProfile(ProfileCreator profileCompletionEntity){
        if (profileCompletionEntity.getStoreName().length() > 50)
            return false;

        String role = String.valueOf(profileCompletionEntity.getRole());
        try{
            Role.valueOf(role);
        }
        catch (IllegalArgumentException e){
            return false;
        }

        Pattern phoneNumberPattern = Pattern.compile("[0-9]{10}");
        Matcher matcher = phoneNumberPattern.matcher(profileCompletionEntity.getPhoneNumber());

        return matcher.find();
    }

    public Pair<Boolean, String> validateUserCreate(UserCreate userCreate){
        if (userCreate.getName().length() > 50 || userCreate.getAddress().length() > 150 || userCreate.getStoreName().length() > 50)
            return new Pair<>(false, "Fields too long");

        String role = String.valueOf(userCreate.getRole());
        try{
            Role.valueOf(role);
        }
        catch (IllegalArgumentException e){
            return new Pair<>(false, "Invalid role");
        }

        Pattern phoneNumberPattern = Pattern.compile("^[0-9]{10}$");
        Matcher matcher = phoneNumberPattern.matcher(userCreate.getPhoneNumber());

        if (matcher.find())
            return new Pair<>(true, "Valid");
        return new Pair<>(false, "Invalid phone number");
    }

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder encoder, EmailSenderService emailSenderService, OTPService otpGenService, GeocodioService geocodioService){
        this.userRepository = userRepository;
        this.encoder = encoder;
        this.otpGenService = otpGenService;
        this.emailSenderService = emailSenderService;
        this.geocodioService = geocodioService;
    }

    public User createUser(User user) {
        user.setPassword(encoder.encode(user.getPassword()));
        var geoResult = geocodioService.geocode(user.getAddress());

        if (geoResult == null)
            return null;

        if (geoResult.getLocation() != null) {
            user.setLatitude(geoResult.getLocation().getLat());
            user.setLongitude(geoResult.getLocation().getLng());
            user.setAddress(geoResult.getFormatted_address() == null ? "" : geoResult.getFormatted_address());
        }
        return userRepository.save(user);
    }

    public void updateUser(User user){
        userRepository.save(user);
    }

    public User findUserByEmail(String email) throws UserNotFoundException {
        return userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException(""));
    }

    public User findUserById(Long userId) throws UserNotFoundException {
        return userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    public boolean userExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> usr = userRepository.findByEmail(username);

        if (usr.isEmpty())
            throw new UsernameNotFoundException("User not found");

        User user = usr.get();
        return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), new ArrayList<>());
    }

    public User extractUserFromAuthentication(Authentication authentication) {
        if (authentication == null)
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login to view your home page");

        Object principal = authentication.getPrincipal();

        User user;
        if (principal instanceof UserDetails userDetails){
            try {
                user = this.findUserByEmail(userDetails.getUsername());
                return user;
            }
            catch (UserNotFoundException ignored){
                return null;
            }
        }
        else if (principal instanceof OAuth2User oAuth2User){
            try {
                user = this.findUserByEmail(oAuth2User.getAttribute("email"));
                return user;
            }
            catch (UserNotFoundException ignored){
                return null;
            }
        }
        return null;
    }

    public List<User> searchSimilar(String query){
        return userRepository.fuzzySearchStoreName(query);
    }

    public List<User> findUsersWithinDistance(Double latitude, Double longitude, Integer maxDistanceKm) {
        List<User> allUsers = userRepository.findAll();

        return allUsers.stream()
                .filter(user -> user.getLatitude() != null && user.getLongitude() != null && user.getRole() != Role.Customer)
                .filter(user -> {
                    double distance = calculateDistance(
                            latitude, longitude,
                            user.getLatitude(), user.getLongitude()
                    );
                    return distance <= maxDistanceKm;
                })
                .collect(Collectors.toList());
    }

    private double calculateDistance(Double lat1, Double lon1, Double lat2, Double lon2) {
        final int EARTH_RADIUS_KM = 6371;

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }
}

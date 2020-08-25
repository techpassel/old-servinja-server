package com.techpassel.servinja.controller;

import com.techpassel.servinja.model.AuthUserDetails;
import com.techpassel.servinja.model.TempUser;
import com.techpassel.servinja.model.User;
import com.techpassel.servinja.repository.TempUserRepo;
import com.techpassel.servinja.repository.UserRepo;
import com.techpassel.servinja.service.AuthService;
import com.techpassel.servinja.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Optional;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("auth")
public class AuthController {

    @Autowired
    UserRepo userRepo;

    @Autowired
    TempUserRepo tempUserRepo;

    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    AuthService authService;

    @Autowired
    JwtUtil jwtUtil;

    @PostMapping("signup")
    public ResponseEntity<?> signup(@RequestBody TempUser user) {
        try {
            String responseMsg = null;
            Optional<User> emailUser = userRepo.findByEmail(user.getEmail());
            if (emailUser.isPresent()) {
                responseMsg = "duplicateEmail";
            }
            Optional<User> phoneUser = userRepo.findByPhone(user.getPhone());
            if (phoneUser.isPresent()) {
                if (responseMsg == "duplicateEmail") {
                    responseMsg = "duplicateEmailAndPhone";
                } else {
                    responseMsg = "duplicatePhone";
                }
            }

            if (responseMsg == null) {
                LocalDateTime dt = LocalDateTime.now();
                tempUserRepo.deleteAllByEmailAndPhone(user.getEmail(), user.getPhone());
                user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
                //This is not a JWT token which we use for user authorization and authentication purpose for http request
                //This is a token which we uses to identify temp users in email verification
                String token = authService.generateToken();
                user.setToken(token);
                user.setUpdatedAt(dt);
                TempUser u = tempUserRepo.save(user);
                responseMsg = "success";
                authService.sendVerificationEmail(user.getEmail(), token);
            }
            return new ResponseEntity<>(responseMsg, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.EXPECTATION_FAILED);
        }
    }

    @PostMapping("verify-email")
    public ResponseEntity<?> verifyEmail(@RequestBody String token) {
        try {
            String responseMsg;
            Optional<TempUser> tempUser = tempUserRepo.findByToken(token);
            if (tempUser.isPresent()) {
                TempUser tUser = tempUser.get();
                LocalDateTime tokenGenerationTime = tUser.getUpdatedAt();
                LocalDateTime oneDayAgoTime = LocalDateTime.now().minusDays(1);
                boolean isBefore = oneDayAgoTime.isBefore(tokenGenerationTime);
                if(isBefore) {
                    User u1 = new User();
                    u1.setFirstName(tUser.getFirstName());
                    u1.setLastName(tUser.getLastName());
                    u1.setEmail(tUser.getEmail());
                    u1.setActive(tUser.isActive());
                    u1.setPhone(tUser.getPhone());
                    u1.setPassword(tUser.getPassword());
                    u1.setRoles(tUser.getRoles());
                    userRepo.save(u1);
                    tempUserRepo.deleteAllByEmailAndPhone(tUser.getEmail(), tUser.getPhone());
                    responseMsg = "success";
                } else {
                    responseMsg = "expire";
                }
            } else {
                responseMsg = "invalid";
            }
            return new ResponseEntity<String>(responseMsg, HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.EXPECTATION_FAILED);
        }
    }

    @PostMapping("signin")
    public ResponseEntity<?> signin(@RequestBody HashMap<String, String> user) {
        try {
            String username = user.get("username");
            String password = user.get("password");
            Optional<User> userData = userRepo.findByEmailOrPhone(username);
            String onboardingStage = "1";
            // Zero mean completed or not required.We will set onboarding stages for customer only.
            // For admin and staff it should be zero now.But later we can add onboarding stages for staffs also.
            HashMap<String, String> res= new HashMap<String, String >();
            if (userData.isPresent()) {
                User uData = userData.get();
                boolean passwordMatches = bCryptPasswordEncoder.matches(password, uData.getPassword());
                if (passwordMatches) {
                    res.put("type", "success");
                    AuthUserDetails authUser = new AuthUserDetails(uData);
                    res.put("token", jwtUtil.generateToken(authUser));
                    res.put("roles", (uData.getRoles()));
                    //Condition for Customer onboardingStage is yet to be completed.
                    res.put("onboardingStage", onboardingStage);
                } else {
                    res.put("type", "incorrectPassword");
                }
            } else {
                Optional<TempUser> tempUser = tempUserRepo.findByEmailOrPhone(username);
                if (tempUser.isPresent()) {
                    res.put("type", "userNotVerified");
                } else {
                    res.put("type","invalidUser");
                }
            }
            System.out.println(res);
            return new ResponseEntity<>(res, HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.EXPECTATION_FAILED);
        }
    }

}

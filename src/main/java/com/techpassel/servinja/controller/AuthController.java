package com.techpassel.servinja.controller;

import com.techpassel.servinja.model.*;
import com.techpassel.servinja.repository.CustomerRepo;
import com.techpassel.servinja.repository.TempUserRepo;
import com.techpassel.servinja.repository.UserRepo;
import com.techpassel.servinja.repository.VerificationTokenRepo;
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
    CustomerRepo customerRepo;

    @Autowired
    VerificationTokenRepo verificationTokenRepo;

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
                tempUserRepo.deleteAllByEmailAndPhone(user.getEmail(), user.getPhone());
                user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
                TempUser u = tempUserRepo.save(user);

                LocalDateTime dt = LocalDateTime.now();
                String token = authService.generateToken();
                VerificationToken v = new VerificationToken(u.getId(), token, VerificationToken.Types.EmailVerificationToken, dt);
                verificationTokenRepo.save(v);
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
            String responseMsg = "invalid";
            Optional<VerificationToken> verificationToken = verificationTokenRepo.findByToken(token);
            if (verificationToken.isPresent()) {
                VerificationToken vt = verificationToken.get();
                LocalDateTime tokenGenerationTime = vt.getGeneratedAt();
                LocalDateTime oneDayAgoTime = LocalDateTime.now().minusDays(1);
                boolean isBefore = oneDayAgoTime.isBefore(tokenGenerationTime);
                if (isBefore) {
                    Optional<TempUser> tempUser = tempUserRepo.findById(vt.getUserId());
                    if (tempUser.isPresent()) {
                        TempUser tUser = tempUser.get();
                        User u1 = new User();
                        u1.setEmail(tUser.getEmail());
                        u1.setActive(true);
                        u1.setPhone(tUser.getPhone());
                        u1.setPassword(tUser.getPassword());
                        u1.setRoles(tUser.getRoles());
                        userRepo.save(u1);
                        verificationTokenRepo.deleteByUserIdAndType(vt.getUserId(), VerificationToken.Types.EmailVerificationToken);
                        tempUserRepo.deleteAllByEmailAndPhone(tUser.getEmail(), tUser.getPhone());
                        Customer customer = new Customer(tUser.getFirstName(), tUser.getLastName());
                        customer.setUser(u1);
                        customerRepo.save(customer);
                        responseMsg = "success";
                    }
                } else {
                    responseMsg = "expire";
                }
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
            int onboardingStage = 0;
            HashMap<String, Object> res = new HashMap<>();
            if (userData.isPresent()) {
                User uData = userData.get();
                boolean passwordMatches = bCryptPasswordEncoder.matches(password, uData.getPassword());
                if (passwordMatches) {
                    Optional<Customer> customer = customerRepo.findByUserId(uData.getId());
                    if (customer.isPresent()) {
                        onboardingStage = customer.get().getOnboardingStage();
                    }
                    res.put("type", "success");
                    AuthUserDetails authUser = new AuthUserDetails(uData);
                    res.put("token", jwtUtil.generateToken(authUser));
                    res.put("roles", (uData.getRoles()));
                    res.put("onboardingStage", onboardingStage);
                } else {
                    res.put("type", "incorrectPassword");
                }
            } else {
                Optional<TempUser> tempUser = tempUserRepo.findByEmailOrPhone(username);
                if (tempUser.isPresent()) {
                    res.put("type", "userNotVerified");
                } else {
                    res.put("type", "invalidUser");
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

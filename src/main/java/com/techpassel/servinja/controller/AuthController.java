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
                Optional<TempUser> t1 = tempUserRepo.findByEmail(user.getEmail());
                if (t1.isPresent()) {
                    TempUser tempEmailUser = t1.get();
                    tempUserRepo.deleteById(tempEmailUser.getId());
                    verificationTokenRepo.deleteByUserId(tempEmailUser.getId());
                }
                Optional<TempUser> t2 = tempUserRepo.findByPhone(user.getPhone());
                if (t2.isPresent()) {
                    TempUser tempPhoneUser = t2.get();
                    tempUserRepo.deleteById(tempPhoneUser.getId());
                    verificationTokenRepo.deleteByUserId(tempPhoneUser.getId());
                }
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

    @PostMapping("resend-verification-email")
    public ResponseEntity<?> resendVerificationEmail(@RequestBody int userId) {
        try {
            HashMap<String, Object> res = new HashMap<>();
            verificationTokenRepo.deleteByUserId(userId);
            Optional<TempUser> tempUser = tempUserRepo.findById(userId);
            if (tempUser.isPresent()) {
                TempUser tUser = tempUser.get();
                String token = authService.generateToken();
                LocalDateTime dt = LocalDateTime.now();
                VerificationToken v = new VerificationToken(userId, token, VerificationToken.Types.EmailVerificationToken, dt);
                verificationTokenRepo.save(v);
                authService.sendVerificationEmail(tUser.getEmail(), token);
                res.put("type", "success");
            } else {
                res.put("type", "invalidUser");
            }
            return new ResponseEntity<>(res, HttpStatus.CREATED);
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

    @PostMapping("send-change-password-link")
    public ResponseEntity<?> sendChangePasswordLink(@RequestBody String username) {
        try {
            HashMap<String, Object> res = new HashMap<>();
            Optional<User> userData = userRepo.findByEmailOrPhone(username);
            if (userData.isPresent()) {
                User user = userData.get();
                verificationTokenRepo.deleteByUserIdAndType(user.getId(), VerificationToken.Types.ChangePasswordToken);
                String token = authService.generateToken();
                LocalDateTime dt = LocalDateTime.now();
                VerificationToken v = new VerificationToken(user.getId(), token, VerificationToken.Types.ChangePasswordToken, dt);
                verificationTokenRepo.save(v);
                authService.sendChangePasswordLinkEmail(user.getEmail(), token);
                res.put("type", "success");
            } else {
                Optional<TempUser> tempUser = tempUserRepo.findByEmailOrPhone(username);
                if (tempUser.isPresent()) {
                    res.put("type", "userNotVerified");
                    res.put("userId", tempUser.get().getId());
                } else {
                    res.put("type", "invalidUser");
                }
            }
            return new ResponseEntity<>(res, HttpStatus.CREATED);
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
                    TempUser tUser = tempUser.get();
                    res.put("type", "userNotVerified");
                    res.put("userId", tUser.getId());
                } else {
                    res.put("type", "invalidUser");
                }
            }
            return new ResponseEntity<>(res, HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.EXPECTATION_FAILED);
        }
    }

    @PostMapping("signin-social-user")
    public ResponseEntity<?> signinSocialUser(@RequestBody HashMap<String, String> user) {
        try {
            String email = user.get("email");
            String firstName = user.get("firstName");
            String lastName = user.get("lastName");
            int onboardingStage = 0;
            HashMap<String, Object> res = new HashMap<>();
            Optional<User> userIfExists = userRepo.findByEmail(email);
            User uData;
            if (userIfExists.isPresent()) {
                //If user already exists.
                uData = userIfExists.get();
                Optional<Customer> customer = customerRepo.findByUserId(uData.getId());
                if (customer.isPresent()) {
                    onboardingStage = customer.get().getOnboardingStage();
                }
            } else {
                Optional<TempUser> tempUser = tempUserRepo.findByEmail(email);
                if (tempUser.isPresent()) {
                    //If User does not exist but TempUser exist.
                    //Creating User from TempUser without verification of email.
                    TempUser tUser = tempUser.get();
                    uData = new User();
                    uData.setEmail(tUser.getEmail());
                    uData.setActive(true);
                    uData.setPhone(tUser.getPhone());
                    uData.setPassword(tUser.getPassword());
                    uData.setRoles(tUser.getRoles());
                    onboardingStage = 1;
                    userRepo.save(uData);
                    verificationTokenRepo.deleteByUserId(tUser.getId());
                    tempUserRepo.deleteAllByEmailAndPhone(tUser.getEmail(), tUser.getPhone());
                    Customer customer = new Customer(tUser.getFirstName(), tUser.getLastName());
                    customer.setUser(uData);
                    customerRepo.save(customer);
                } else {
                    //If neither User not TempUser exist.
                    // Create new User with given information by third party(Gmail or facebook).
                    uData = new User();
                    uData.setEmail(email);
                    uData.setActive(true);
                    onboardingStage = 1;
                    //In future when more type of users will be added, then it needs to be managed.
                    uData.setRoles("customer");
                    userRepo.save(uData);
                    Customer customer = new Customer(firstName, lastName);
                    customer.setUser(uData);
                    customerRepo.save(customer);
                }
            }
            res.put("type", "success");
            AuthUserDetails authUser = new AuthUserDetails(uData);
            res.put("token", jwtUtil.generateToken(authUser));
            res.put("roles", (uData.getRoles()));
            res.put("onboardingStage", onboardingStage);
            return new ResponseEntity<>(res, HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.EXPECTATION_FAILED);
        }
    }

    @PostMapping("set-admin")
    public ResponseEntity<String> setAdmin(@RequestBody HashMap<String, String> data) {
        try {
            String email = data.get("email");
            String password = data.get("password");
            String encryptedPassword = bCryptPasswordEncoder.encode(password);
            Optional<User> userIfExist = userRepo.findByRoles("admin");
            User u1;
            if (userIfExist.isPresent()) {
                u1 = userIfExist.get();
            } else {
                u1 = new User();
                u1.setActive(true);
                u1.setRoles("admin");
            }
            u1.setEmail(email);
            u1.setPassword(encryptedPassword);
            userRepo.save(u1);
            return new ResponseEntity<String>("success", HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<String>("error", HttpStatus.EXPECTATION_FAILED);
        }
    }

    // Used when request is sent in "get-reset-password-token-details?token=xyz" format
    //    @GetMapping("get-reset-password-token-details")
    //    public ResponseEntity<?> getResetPasswordTokenDetails(@RequestParam String token){
    @GetMapping("get-reset-password-token-details/{token}")
    public ResponseEntity<?> getResetPasswordTokenDetails(@PathVariable("token") String token) {
        try {
            HashMap<String, Object> res = new HashMap<>();
            Optional<VerificationToken> verificationToken = verificationTokenRepo.findByToken(token);
            if (verificationToken.isPresent()) {
                VerificationToken vt = verificationToken.get();
                LocalDateTime tokenGenerationTime = vt.getGeneratedAt();
                LocalDateTime oneDayAgoTime = LocalDateTime.now().minusDays(1);
                boolean isBefore = oneDayAgoTime.isBefore(tokenGenerationTime);
                if (isBefore) {
                    res.put("type", "valid");
                } else {
                    res.put("type","expired");
                }
            } else {
                res.put("type","invalid");
            }
            return new ResponseEntity<>(res, HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.EXPECTATION_FAILED);
        }
    }

    @PostMapping("reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody HashMap<String, String> data){
        try {
            HashMap<String, Object> res = new HashMap<>();
            String token = data.get("token");
            String password = data.get("password");
            Optional<VerificationToken> verificationToken = verificationTokenRepo.findByToken(token);
            if (verificationToken.isPresent()) {
                VerificationToken vt = verificationToken.get();
                int userId = vt.getUserId();
                LocalDateTime tokenGenerationTime = vt.getGeneratedAt();
                LocalDateTime oneDayAgoTime = LocalDateTime.now().minusDays(1);
                boolean isBefore = oneDayAgoTime.isBefore(tokenGenerationTime);
                if (isBefore) {
                    String encryptedPassword = bCryptPasswordEncoder.encode(password);
                    userRepo.updatePassword(userId,encryptedPassword);
                    verificationTokenRepo.deleteByUserIdAndType(userId, VerificationToken.Types.ChangePasswordToken);
                    res.put("type","success");
                } else {
                    res.put("type","expired");
                }
            } else {
                res.put("type","invalid");
            }
            return new ResponseEntity<>(res, HttpStatus.OK);
        } catch (Exception e){
            e.printStackTrace();
            return  new ResponseEntity<>(null, HttpStatus.EXPECTATION_FAILED);
        }
    }
}

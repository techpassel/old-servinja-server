package com.techpassel.servinja.controller;

import com.techpassel.servinja.model.Customer;
import com.techpassel.servinja.model.User;
import com.techpassel.servinja.model.VerificationToken;
import com.techpassel.servinja.repository.CustomerRepo;
import com.techpassel.servinja.repository.UserRepo;
import com.techpassel.servinja.repository.VerificationTokenRepo;
import com.techpassel.servinja.service.CommunicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "http://localhost:6250")
@RestController
@RequestMapping("onboard")
public class OnboardingController {

    @Autowired
    UserRepo userRepo;

    @Autowired
    CustomerRepo customerRepo;

    @Autowired
    CommunicationService commService;

    @Autowired
    VerificationTokenRepo verificationTokenRepo;

    @RequestMapping(value = "get-customer-details", method = RequestMethod.GET)
    public ResponseEntity<?> getCustomerDetails(@RequestParam("userId") String userId) {
        //@RequestMapping(value = "get-user-details/{userId}", method = RequestMethod.GET)
        //public ResponseEntity<?> getUserDetails(@PathVariable("userId") String userId) {
        try {
            int id = Integer.parseInt(userId);
            Optional<Customer> customerIfExist = customerRepo.findByUserId(id);
            HashMap h = new HashMap();
            h.put("type", "not-found");
            if (customerIfExist.isPresent()) {
                Customer user = customerIfExist.get();
                h.put("type", "success");
                h.put("customer", user);
            }
            return new ResponseEntity<>(h, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.EXPECTATION_FAILED);
        }
    }

    @PostMapping("update-onboarding-stage")
    public ResponseEntity<?> updateOnboardingStage(@RequestBody HashMap<String, Integer> data) {
        try {
            int userId = data.get("userId");
            int newOnboardingStage = data.get("newOnboardingStage");
            HashMap h = new HashMap();
            h.put("type", "not-found");
            Optional<Customer> customerIfExist = customerRepo.findByUserId(userId);
            if (customerIfExist.isPresent()) {
                Customer customer = customerIfExist.get();
                customer.setOnboardingStage(newOnboardingStage);
                customerRepo.save(customer);
                h.put("type", "success");
                h.put("newOnboardingStage", newOnboardingStage);
                //onBoarding status will be stored in customer table data so will update in that table
            }
            return new ResponseEntity<>(h, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.EXPECTATION_FAILED);
        }
    }

    @PostMapping("save-profile-completion-data")
    public ResponseEntity<?> saveProfileCompletionData(@RequestBody HashMap<String, Object> data) {
        try {
            int id = (Integer) data.get("id");
            Optional<User> userIfExist = userRepo.findById(id);
            String responseType = "error";
            if (userIfExist.isPresent()) {
                User u = userIfExist.get();
                String phone = (String) data.get("phone");
                Optional<User> duplicatePhone = userRepo.findOtherUserByPhone(phone, id);
                if (duplicatePhone.isPresent()) {
                    responseType = "duplicatePhone";
                } else {
                    u.setPhone(phone);
                    userRepo.save(u);
                    String firstname = (String) data.get("firstName");
                    String lastname = (String) data.get("lastName");
                    int age = (Integer) data.get("age");
                    String gender = (String) data.get("gender");
                    Optional<Customer> customerIfExist = customerRepo.findByUserId(id);
                    if (customerIfExist.isPresent()) {
                        Customer customer = customerIfExist.get();
                        customer.setAge(age);
                        customer.setFirstName(firstname);
                        customer.setLastName(lastname);
                        customer.setGender(gender);
                        customer.setOnboardingStage(2);
                        customerRepo.save(customer);
                        responseType = "success";
                    }
                }
            }
            return new ResponseEntity<>(responseType, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.EXPECTATION_FAILED);
        }
    }

    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@RequestBody HashMap<String, Object> userData){
        try{
            int userId = (Integer) userData.get("userId");
            String phone = (String) userData.get("phone");
            String responseType;
            Map smsResponse = new HashMap();
            try {
                smsResponse = commService.sendOtp(phone);
                responseType = (String) smsResponse.get("status");
            } catch (Exception e){
                e.printStackTrace();
                responseType = "error";
            }
            if(responseType == "success") {
                LocalDateTime l = LocalDateTime.now();
                String token = smsResponse.get("token").toString();
                VerificationToken v = new VerificationToken(userId, token, VerificationToken.Types.PhoneVerificationToken, l);
                this.verificationTokenRepo.save(v);
            }
            return new ResponseEntity<>(responseType, HttpStatus.OK);
        } catch (Exception e){
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.EXPECTATION_FAILED);
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody HashMap<String, Object> userData){
        try{
            String token = userData.get("token").toString();
            int userId = (Integer) userData.get("userId");
            String responseType = "error";
            Optional<VerificationToken> tokenIfExist = verificationTokenRepo.findByTokenAndUserId(token, userId);
            if(tokenIfExist.isPresent()){
                VerificationToken vt = tokenIfExist.get();
                LocalDateTime generationTime = vt.getGeneratedAt();
                //Taking 16 in place of 15 as taking one minute as buffer time.
                LocalDateTime timeFifteenMinutesAgo = LocalDateTime.now().minusMinutes(16);
                if (generationTime.isAfter(timeFifteenMinutesAgo)){
                    try {
                        Optional<Customer> customerIfExist = customerRepo.findByUserId(userId);
                        if(customerIfExist.isPresent()) {
                            Customer customer = customerIfExist.get();
                            if(customer.getOnboardingStage() == 2){
                                customer.setOnboardingStage(3);
                            }
                            customer.setPhoneVerified(true);
                            customerRepo.save(customer);
                            responseType = "success";
                        }
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                } else {
                    responseType = "invalid";
                }
                verificationTokenRepo.deleteByUserIdAndType(userId, VerificationToken.Types.PhoneVerificationToken);
            }
            return new ResponseEntity<>(responseType, HttpStatus.OK);
        } catch (Exception e){
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.EXPECTATION_FAILED);
        }
    }

}

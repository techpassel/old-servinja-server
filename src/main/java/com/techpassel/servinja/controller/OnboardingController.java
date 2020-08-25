package com.techpassel.servinja.controller;

import com.techpassel.servinja.model.TempUser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:6250")
@RestController
@RequestMapping("onboard")
public class OnboardingController {
    @PostMapping("testUrl")
    public ResponseEntity<?> test() {
        try {
            System.out.println("Method called");
            String responseMsg = "Precessed Successfully.";
            System.out.println("response returned");
            return new ResponseEntity<>(responseMsg, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.EXPECTATION_FAILED);
        }
    }
}

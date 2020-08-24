package com.techpassel.servinja.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Random;

@Service
public class AuthService {
    @Autowired
    EmailService emailService;

    @Value("${techpassel.app.clientBaseUrl}")
    String clientBaseUrl;

    @Value("${techpassel.app.name}")
    String appName;

    @Async
    public void sendVerificationEmail(String email, String token) throws InterruptedException {
        String sub = "Please verify your email address.";
        String baseUrl = clientBaseUrl+ "/verify-email/" + token;
        String style = "\"background-color:red; color:white; border:none; cursor: pointer; font-weight: bold; " +
                "border-radius:10px; text-decoration: none; font-size: 16px; padding: 18px 24px;\"";
        String content = "<h1>Welcome to "+appName+"</h1><br><br><p>To complete your "+appName+" sign up, we just need to verify your email address. Please click on the button below to verify your email" +
                "</p><br><a href="+ baseUrl + "><button style=" + style + ">Verify email address</button></a>";
        boolean isEmailSent = emailService.sendEmailWithAttachment(email, sub, content);
        //Retrying to send email if failed in previous attempt
        if (!isEmailSent) {
            //Code to make current thread sleep for 2 minutes.
            Thread.sleep(120000);
            emailService.sendEmailWithAttachment(email, sub, content);
        }
    }

    public String generateToken(){
        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 22;
        Random random = new Random();

        String token = random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
        return token;
    }
}

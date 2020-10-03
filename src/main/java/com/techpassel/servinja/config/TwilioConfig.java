package com.techpassel.servinja.config;

import com.twilio.Twilio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TwilioConfig {
    public static final String ACCOUNT_SID = "AC9e455cd06e5e5295a84d07e045985d39";
    public static final String AUTH_TOKEN = "9af8fc9943cf1b595f87e4260539cc25";

    @Autowired
    public TwilioConfig(){
        try {
            Twilio.init(ACCOUNT_SID,AUTH_TOKEN);
            System.out.println("Successfully connected to twilio");
        } catch (Exception e){
            System.out.println(e.getMessage());
            System.out.println("Could not connect to twilio");
        }

    }
}

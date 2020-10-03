package com.techpassel.servinja.service;

import com.techpassel.servinja.model.SmsDetails;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CommunicationService {

    @Value("${twilio.default_number}")
    String TWILIO_NUMBER;

    public List sendSms(SmsDetails smsDetails){
            String message = smsDetails.getMessage();
            List<String> phoneNumbers = smsDetails.getPhoneNumbers();
            PhoneNumber senderNumber = new PhoneNumber(TWILIO_NUMBER);
            List<HashMap<String, Object>> results = new ArrayList<>();
            ListIterator ltr = phoneNumbers.listIterator();
            if(ltr.hasNext()){
                String num = (String) ltr.next();
                PhoneNumber phoneNumber = new PhoneNumber("+91" + num);
                HashMap h = new HashMap();
                h.put("phone", num);
                try {
                    Message sms = Message.creator(phoneNumber, senderNumber, message).create();
                    System.out.println(sms.getSid());
                    h.put("status", "success");
                    h.put("sid", sms.getSid());
                } catch (Exception e) {
                    h.put("status", "error");
                }
                results.add(h);
            }
            return results;
    }

    public Map sendOtp(String phone){
        int otp = new Random().nextInt(900000) + 100000;
        String message = "Your Servinja verification code is : "+otp;
        List<String> phones = new ArrayList<>();
        phones.add(phone);
        SmsDetails sms = new SmsDetails(phones, message);
        List l = this.sendSms(sms);
        Map h = (HashMap) l.get(0);
        h.put("token", otp);
        return h;
    }
}

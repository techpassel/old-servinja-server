package com.techpassel.servinja.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    JavaMailSender mailSender;

    @Value("${techpassel.app.name}")
    String sender;

    public boolean sendEmailWithAttachment(String email,String subject,String content){
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
            mimeMessageHelper.setSubject(subject);
            mimeMessageHelper.setFrom("Servinja");
            mimeMessageHelper.setTo(email);
            mimeMessageHelper.setText(content, true);
            //Second parameter to is set content type as HTML and not plain text.
            mailSender.send(mimeMessageHelper.getMimeMessage());
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

}

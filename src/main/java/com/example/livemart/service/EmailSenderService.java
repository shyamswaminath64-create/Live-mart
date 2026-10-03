package com.example.livemart.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

@Service
public class EmailSenderService {
    private static String user = "lively4mart@gmail.com";
    private  static String password = "zmrfjzcewsydobpo";

    @Async
    public void sendEmail(String to, String from , String subject, String text){
        boolean flag=false;

        Properties properties=new Properties();
        properties.put("mail.smtp.auth",true);
        properties.put("mail.smtp.starttls.enable",true);
        properties.put("mail.smtp.port","587");
        properties.put("mail.smtp.host","smtp.gmail.com");

        Session session=Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user,password);
            }
        });

        try {
            Message message=new MimeMessage(session);

            message.setRecipient(Message.RecipientType.TO,new InternetAddress(to));
            message.setFrom(new InternetAddress(from));
            message.setSubject(subject);
            message.setText(text);

            Transport.send(message);
            flag=true;

        }catch (Exception e){
            System.out.println("Unable to send email");
        }
    }
}
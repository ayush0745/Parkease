package com.parkease.notification.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.Account;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SmsService {

    private static final String PLACEHOLDER_ACCOUNT_SID = "your-account-sid";

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.phone-number}")
    private String twilioPhoneNumber;

    @PostConstruct
    public void initTwilio() {
        if (isPlaceholderCredentials()) {
            System.out.println("Twilio credentials are placeholders; SMS sending is disabled.");
            return;
        }
        Twilio.init(accountSid, authToken);
    }

    public void sendSms(String phoneNumber, String message) {
        if (isPlaceholderCredentials()) {
            throw new RuntimeException("SMS sending disabled: configure Twilio credentials first");
        }

        try {
            Message msg = Message.creator(
                    new PhoneNumber(phoneNumber),
                    new PhoneNumber(twilioPhoneNumber),
                    message
            ).create();

            System.out.println("SMS sent successfully to: " + phoneNumber + " with SID: " + msg.getSid());
        } catch (Exception e) {
            System.err.println("Failed to send SMS: " + e.getMessage());
            throw new RuntimeException("SMS sending failed: " + e.getMessage());
        }
    }

    private boolean isPlaceholderCredentials() {
        return PLACEHOLDER_ACCOUNT_SID.equals(accountSid)
                || authToken == null
                || authToken.isBlank()
                || twilioPhoneNumber == null
                || twilioPhoneNumber.isBlank();
    }
}

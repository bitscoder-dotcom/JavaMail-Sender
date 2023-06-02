package dev.roman.javamail.event.listener;

import dev.roman.javamail.event.RegistrationCompleteEvent;
import dev.roman.javamail.exception.CustomException;
import dev.roman.javamail.model.User;
import dev.roman.javamail.service.UserService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class RegistrationCompleteEventListener implements
        ApplicationListener<RegistrationCompleteEvent> {
    private final UserService userService;
    private final JavaMailSender mailSender;
    private User theUser;

    @Override
    public void onApplicationEvent(RegistrationCompleteEvent event) {
/*
Steps to follow to actually send the email:-
    1.  Get the newly registered user
    2.  Create a verification token for the user
    3.  Save the verification token for the user
    4.  Build the verification url to be sent to the user
    5.  Send the email.
 */
        theUser = event.getUser(); // 1
        String verificationToken = UUID.randomUUID().toString(); // 2
        userService.saveUserVerificationToken(theUser, verificationToken); // 3
        String url = event.getApplicationUrl()+"/register/verifyEmail?token=" +
                verificationToken; // 4
        try {
            sendVerificationEmail(url);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } // 5
        log.info("Click the link to verify your email and complete registration" +
                ": {}", url);
    }

    @Async
    public void sendVerificationEmail(String url) throws MessagingException, UnsupportedEncodingException {
        String subject = "Email Verification";
        String senderName = "User Registration Portal Service";
        String mailContent = "<p> Hi, " + theUser.getFirstName() + ", </p>" +
                "<p>Thank you for registering with us," + "" +
                "Please, follow the link below to complete your registration. </p>" +
                "<a href=\"" + url + "\">Verify your email to activate your account</a>" +
                "<<p> Thank you <br> Users Registration Portal Service";

        MimeMessagePreparator mimeMessagePreparator = mimeMessage -> {
            var messageHelper = new MimeMessageHelper(mimeMessage);
            messageHelper.setFrom("accountregistration@mail.com", senderName);
            messageHelper.setTo(theUser.getEmail());
            messageHelper.setSubject(subject);
            messageHelper.setText(mailContent, true);
        };

        try {
            CompletableFuture.runAsync(() ->
                    mailSender.send(mimeMessagePreparator)).exceptionally(exp -> {
                throw new CustomException("Exception occurred sending mail [message]: " + exp.getLocalizedMessage());
            });
            log.info("email has sent!!");
        } catch (MailException exception) {
            log.error("Exception occurred when sending mail {}", exception.getMessage());
            throw new CustomException("Exception occurred when sending mail to " + HttpStatus.EXPECTATION_FAILED);
        }
    }
}

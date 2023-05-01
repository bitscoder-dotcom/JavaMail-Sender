package dev.roman.javamail.registration;

import dev.roman.javamail.event.RegistrationCompleteEvent;
import dev.roman.javamail.model.User;
import dev.roman.javamail.registration.token.VerificationToken;
import dev.roman.javamail.registration.token.VerificationTokenRepository;
import dev.roman.javamail.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/register")
@RequiredArgsConstructor
public class RegistrationController {
    private final UserService userService;
    private final ApplicationEventPublisher publisher;
    private final VerificationTokenRepository tokenRepository;

    @PostMapping
    public String registerUser(@RequestBody RegistrationRequest registrationRequest,
                               final HttpServletRequest request) {
        User user = userService.registerUser(registrationRequest);
// After the user has been saved to the database, we publish an event
        publisher.publishEvent(new RegistrationCompleteEvent(user,
                applicationUrl(request)));
        return "Success! Pleases, check your email for registration link";
    }

    public String applicationUrl(HttpServletRequest request) {
        return "http://" + request.getServerName() + ":" + request.getServerPort()
                + request.getContextPath();
    }

/*
The code above :-
    registers a user to the database, when the user is saved successfully,
    an event(RegistrationCompleteEvent) will be published using the
    ApplicationEventPublisher

    To send an email once the event has been successfully published, we create
    a listener that will listen to this event publication and send the email
 */

    @GetMapping("/verifyEmail")
    public String verifyEmail(@RequestParam("token") String token) {
        VerificationToken theToken = tokenRepository.findByToken(token);
        if (theToken.getUser().isEnabled()) {
            return "Account has already been verified, Please login";
        }
        String verificationResult = userService.validateToken(token);
        if (verificationResult.equalsIgnoreCase("User validated")) {
            return "Email verified successfully. Now you can login";
        }
        return "Invalid verification token";
    }
}
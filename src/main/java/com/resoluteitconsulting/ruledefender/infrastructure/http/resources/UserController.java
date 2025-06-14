package com.resoluteitconsulting.ruledefender.infrastructure.http.resources;

import com.resoluteitconsulting.ruledefender.infrastructure.database.model.UserOrganization;
import com.resoluteitconsulting.ruledefender.domain.model.OrganizationUser;
import com.resoluteitconsulting.ruledefender.domain.model.UserActivationRequest;
import com.resoluteitconsulting.ruledefender.infrastructure.database.repository.UserOrganizationRepository;
import com.resoluteitconsulting.ruledefender.domain.usecases.EmailService;
import com.resoluteitconsulting.ruledefender.domain.usecases.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.Optional;

@RestController
@Slf4j
@AllArgsConstructor
@RequestMapping(("/user"))
public class UserController {

    private final UserOrganizationRepository userOrganizationRepository;
    private final UserService userService;
    private final EmailService emailService;

    @PostMapping
    public ResponseEntity<Void> createUser(@RequestBody OrganizationUser organizationUser) {

        String createdUserId = userService.createUser(organizationUser);

        log.info("Created User ID: {}", createdUserId);
        String activationCode = userService.generateRandomPassword(10);

        UserOrganization userOrganization = new UserOrganization();
        userOrganization.setUserId(createdUserId);
        userOrganization.setStatus(true);
        userOrganization.setActivated(false);
        userOrganization.setEmail(organizationUser.getEmail());
        userOrganization.setActivationCode(activationCode);
        userOrganization.setActivationSendDate(OffsetDateTime.now());
        userOrganizationRepository.save(userOrganization);

        emailService.sendSimpleEmail(organizationUser.getEmail(), activationCode);

        return ResponseEntity.status(201).build();
    }

    @PostMapping("/activate")
    public ResponseEntity<Void> activateUser(@RequestBody UserActivationRequest organizationUser) {

        Optional<UserOrganization> user = userOrganizationRepository.findByEmail(organizationUser.getEmail());

        if (user.isEmpty())
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "No user found with the provided email"
            );

        if (StringUtils.equals(organizationUser.getActivationCode(), user.get().getActivationCode())) {
            user.get().setActivated(true);
            user.get().setActivationDate(OffsetDateTime.now());
            userOrganizationRepository.save(user.get());
            return ResponseEntity.ok().build();
        }

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid code provided");

    }

    @PostMapping("/resend")
    public ResponseEntity<Void> resendActivationCodeUser(@RequestBody UserActivationRequest organizationUser) {

        Optional<UserOrganization> user = userOrganizationRepository.findByEmail(organizationUser.getEmail());

        if (user.isEmpty())
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "No user found with the provided email"
            );
        emailService.sendSimpleEmail(organizationUser.getEmail(), user.get().getActivationCode());

        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset")
    public ResponseEntity<Void> resetUserPassword(@RequestBody OrganizationUser organizationUser) {
        userService.resetUserPassword(organizationUser);
        return ResponseEntity.accepted().build();
    }


}

package com.resoluteitconsulting.ruledefender.infrastructure.http.service;

import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.microsoft.graph.authentication.TokenCredentialAuthProvider;
import com.microsoft.graph.models.AuthenticationMethodResetPasswordParameterSet;
import com.microsoft.graph.models.ObjectIdentity;
import com.microsoft.graph.models.PasswordProfile;
import com.microsoft.graph.models.User;
import com.microsoft.graph.requests.GraphServiceClient;
import com.resoluteitconsulting.ruledefender.domain.model.OrganizationUser;
import com.resoluteitconsulting.ruledefender.domain.usecases.UserService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Request;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private static final List<String> SCOPES = List.of("https://graph.microsoft.com/.default");

    @Value("${app.user.client-id}")
    private String clientId;

    @Value("${app.user.tenant-id}")
    private String tenantId;

    @Value("${app.user.secret}")
    private String clientSecret;

    @Value("${app.user.issuer}")
    private String issuer;


    @Override
    public String createUser(OrganizationUser organizationUserToCreate) {
        try {

            log.info("Calling Azure GRAPH API");

            final ClientSecretCredential credential = new ClientSecretCredentialBuilder()
                    .clientId(clientId)
                    .tenantId(tenantId)
                    .clientSecret(clientSecret)
                    .build();


            final TokenCredentialAuthProvider authProvider = new TokenCredentialAuthProvider(SCOPES, credential);

            GraphServiceClient<Request> graphClient = GraphServiceClient.builder().authenticationProvider(authProvider).buildClient();

            var user = new User();
            user.accountEnabled = true;
            user.displayName = organizationUserToCreate.getFullName();
            user.mailNickname = organizationUserToCreate.getUsername();

            PasswordProfile passwordProfile = new PasswordProfile();
            passwordProfile.forceChangePasswordNextSignIn = false;
            passwordProfile.password = organizationUserToCreate.getPassword();
            user.passwordProfile = passwordProfile;

            var objectIdentity = new ObjectIdentity();
            objectIdentity.signInType = "emailAddress";
            objectIdentity.issuer = issuer;
            objectIdentity.issuerAssignedId = organizationUserToCreate.getEmail();
            user.identities = List.of(
                    objectIdentity
            );

            var createdUser = graphClient.users()
                    .buildRequest()
                    .post(user);

            log.info("User created successfully, ID={}", createdUser);

            return createdUser.id;

        } catch (Exception ex) {
            log.error("Exception occurred: ", ex);
            throw ex;
        }
    }

    @Override
    public String createUserWithoutPassword(OrganizationUser organizationUserToCreate) {
        try {

            final ClientSecretCredential credential = new ClientSecretCredentialBuilder()
                    .clientId(clientId)
                    .tenantId(tenantId)
                    .clientSecret(clientSecret)
                    .build();


            final TokenCredentialAuthProvider authProvider = new TokenCredentialAuthProvider(SCOPES, credential);

            GraphServiceClient<Request> graphClient = GraphServiceClient.builder().authenticationProvider(authProvider).buildClient();

            var user = new User();
            user.accountEnabled = true;
            user.displayName = organizationUserToCreate.getFullName();
            user.mailNickname = organizationUserToCreate.getUsername();

            PasswordProfile passwordProfile = new PasswordProfile();
            passwordProfile.forceChangePasswordNextSignIn = true;
            passwordProfile.password = generateRandomPassword(10);
            user.passwordProfile = passwordProfile;

            var objectIdentity = new ObjectIdentity();
            objectIdentity.signInType = "emailAddress";
            objectIdentity.issuer = issuer;
            objectIdentity.issuerAssignedId = organizationUserToCreate.getEmail();
            user.identities = List.of(
                    objectIdentity
            );

            var createdUser = graphClient.users()
                    .buildRequest()
                    .post(user);

            log.info("User created successfully, ID={}", createdUser);

            return createdUser.id;

        } catch (Exception ex) {
            log.error("Exception occurred: ", ex);
            throw ex;
        }
    }

    @Override
    public void resetUserPassword(OrganizationUser organizationUser) {
        final ClientSecretCredential credential = new ClientSecretCredentialBuilder()
                .clientId(clientId)
                .tenantId(tenantId)
                .clientSecret(clientSecret)
                .build();

        final TokenCredentialAuthProvider authProvider = new TokenCredentialAuthProvider(SCOPES, credential);

        GraphServiceClient<Request> graphClient = GraphServiceClient.builder().authenticationProvider(authProvider).buildClient();

        AuthenticationMethodResetPasswordParameterSet authenticationMethodResetPasswordParameterSet = new AuthenticationMethodResetPasswordParameterSet();
        authenticationMethodResetPasswordParameterSet.newPassword = organizationUser.getPassword();

        var result = graphClient
                .users()
                .byId(organizationUser.getUserId())
                .authentication()
                .emailMethods(organizationUser.getEmail())
                .resetPassword(authenticationMethodResetPasswordParameterSet)
                ;

        log.info("Result: {}", result);

    }

    public String generateRandomPassword(int length) {
        String upperCaseLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowerCaseLetters = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String specialCharacters = "!@#$%^&*()-_+=<>?";

        // Combine all character sets
        String allCharacters = upperCaseLetters + lowerCaseLetters + digits + specialCharacters;

        // Secure random generator
        SecureRandom random = new SecureRandom();

        // Ensure at least one character from each set is included
        StringBuilder password = new StringBuilder();
        password.append(upperCaseLetters.charAt(random.nextInt(upperCaseLetters.length())));
        password.append(lowerCaseLetters.charAt(random.nextInt(lowerCaseLetters.length())));
        password.append(digits.charAt(random.nextInt(digits.length())));
        password.append(specialCharacters.charAt(random.nextInt(specialCharacters.length())));

        // Fill the remaining characters
        for (int i = 4; i < length; i++) {
            password.append(allCharacters.charAt(random.nextInt(allCharacters.length())));
        }

        // Shuffle the characters to avoid predictable patterns
        List<Character> passwordCharacters = new ArrayList<>();
        for (char c : password.toString().toCharArray()) {
            passwordCharacters.add(c);
        }
        Collections.shuffle(passwordCharacters);

        // Convert the shuffled list back to a string
        StringBuilder finalPassword = new StringBuilder();
        for (char c : passwordCharacters) {
            finalPassword.append(c);
        }

        return finalPassword.toString();
    }
}

package com.api.service;

import com.api.entities.*;
import com.api.env.resources.AppResources;
import com.api.mapper.UserMapper;
import com.api.model.*;
import com.api.output.UserJSON;
import com.api.repository.*;
import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import com.crypto.Crypt;
import com.crypto.PasswordHash;
import com.api.template.Templates;
import com.email.service.EmailManager;
import com.internationalization.EmailMessages;
import com.internationalization.Messages;
import com.util.enums.HTTPCustomStatus;
import com.util.exceptions.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.GeneralSecurityException;
import java.util.*;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);


    private final UserRepository userRepository;
    private final EmailManager emailManager;

    @Autowired
    public UserService(UserRepository userRepository, EmailManager emailManager) {

        this.userRepository = userRepository;
        this.emailManager = emailManager;
    }

    @Transactional
    public UserJSON save(UserInput input, com.util.enums.Language language) throws GeneralSecurityException {

        final User user = UserMapper.inputToUser(input);
        final User result = saveUser(user, input, language);

        String validationKey = Crypt.encrypt(NanoIdUtils.randomNanoId(), AppResources.ENCRYPTION_KEY.value());
        String encryptedUserKey = Crypt.encrypt(result.getUserKey(), AppResources.ENCRYPTION_KEY.value());

        String url = AppResources.ACCOUNT_CONFIRMATION_URL.value() + "/" + validationKey + "/" + encryptedUserKey;

        Map<String, Object> templateVariables = new HashMap<>();
        templateVariables.put("fullName", result.getFullName());
        templateVariables.put("confirmationLink", url);
        emailManager.send(result.getEmail(), EmailMessages.get("new_user.subject", language.getLocale()), Templates.NEW_USER, templateVariables, language.getLocale());

        return UserMapper.userToJson(result);

    }

    @Transactional
    public boolean checkAvailabilityByEmail(String email) {

        Optional<User> user = userRepository.findByEmail(email);
        return !user.isPresent();
    }

    @Transactional
    public boolean checkEmailExists(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        return user.isPresent();
    }


    @Transactional
    public UserJSON loadUser(String email, com.util.enums.Language language) throws ApiException {

        final User user = userRepository.findByEmail(email).orElseThrow(
                () -> new ApiException(Messages.get("USER.NOT.EXIST", language), HTTPCustomStatus.UNAUTHORIZED)
        );

        logger.info("User with email={} loaded", email);
        return UserMapper.userToJson(user);

    }


    private User saveUser(User user, UserInput input, com.util.enums.Language language) {

        String uuid = NanoIdUtils.randomNanoId();
        user.setUserKey(uuid);

        userRepository.save(user);
        if (user.getId() > 0) {
            logger.debug("Start password hashing");
            String password = PasswordHash.encode(input.getPassword());
            logger.debug("Finished password hashing");

            user.setPassword(password);
        }

        return user;
    }

}
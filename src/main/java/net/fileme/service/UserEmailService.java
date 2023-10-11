package net.fileme.service;

import net.fileme.domain.dto.TokenDto;
import net.fileme.domain.pojo.User;
import net.fileme.enums.EmailTemplateEnum;

public interface UserEmailService{
    User createUser(User guest);
    void sendSignUpEmail(EmailTemplateEnum templateEnum, String email);
    void processSignUp(String token);
    void sendChangeEmail(EmailTemplateEnum templateEnum, Long userId, String reqEmail);
    TokenDto processChangeEmail(String token);
    void deleteToken(String token);
    void setUserState(EmailTemplateEnum templateEnum, Long userId);
    void createBasicEmail(EmailTemplateEnum templateEnum, String emailReceiver);
}

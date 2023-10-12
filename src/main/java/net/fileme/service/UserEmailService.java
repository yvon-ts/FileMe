package net.fileme.service;

import net.fileme.domain.dto.TokenDto;
import net.fileme.domain.pojo.User;
import net.fileme.enums.EmailTemplateEnum;

public interface UserEmailService{
    User createUser(User guest);
    void sendTokenEmail(EmailTemplateEnum templateEnum, String emailReceiver, String pending);
//    void sendSignUpEmail(EmailTemplateEnum templateEnum, String email);
    void processSignUp(String token);
//    void sendChangeEmail(EmailTemplateEnum templateEnum, String oldEmail, String reqEmail);
    TokenDto processChangeEmail(String token);
    TokenDto processResetPwd(String rawPwd, String token);
    void deleteToken(String token);
    void setUserState(EmailTemplateEnum templateEnum, String email);
    void createBasicEmail(EmailTemplateEnum templateEnum, String emailReceiver);
}

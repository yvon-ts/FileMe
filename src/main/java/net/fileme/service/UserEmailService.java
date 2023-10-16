package net.fileme.service;

import net.fileme.domain.dto.TokenDto;
import net.fileme.domain.pojo.User;
import net.fileme.enums.EmailTemplateEnum;

public interface UserEmailService{
    // ----------------------------Sign Up---------------------------- //
    User createUser(User guest);
    void processSignUp(String token);
    // ----------------------------Change Email---------------------------- //
    TokenDto processChangeEmail(String token);
    // ----------------------------Reset Password---------------------------- //
    TokenDto processResetPassword(String rawPassword, String token);
    // ----------------------------Util Methods---------------------------- //
    void setUserState(EmailTemplateEnum templateEnum, String email);
    void sendTokenEmail(EmailTemplateEnum templateEnum, String emailReceiver, String pending);
    void createBasicEmail(EmailTemplateEnum templateEnum, String emailReceiver);
    void deleteToken(String token);
}

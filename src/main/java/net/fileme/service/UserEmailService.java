package net.fileme.service;

import net.fileme.domain.dto.TokenDto;
import net.fileme.domain.dto.UserDto;
import net.fileme.domain.pojo.User;
import net.fileme.enums.EmailTemplateEnum;

public interface UserEmailService{
    // ----------------------------Sign Up---------------------------- //
    User createUser(UserDto guest);
    void processSignUp(String token);

    // ----------------------------Change Password----------------------------- //
    void matchCurrentPwd(String pwdFromUser, String pwdFromDb);
    void processChangePwd(Long userId, String newPwd);

    // ----------------------------Change Email---------------------------- //
    TokenDto processChangeEmail(String token);
    // ----------------------------Reset Password---------------------------- //
    TokenDto processResetPassword(String rawPassword, String token);
    // ----------------------------Util Methods---------------------------- //
    void setUserState(EmailTemplateEnum templateEnum, String email);
    void sendTokenEmail(EmailTemplateEnum templateEnum, String emailReceiver, String pending);
    void sendTokenEmailForEmailChange(EmailTemplateEnum templateEnum, String emailReceiver, String pending);
    void createBasicEmail(EmailTemplateEnum templateEnum, String emailReceiver);

    TokenDto lookUpToken(String token);

    void deleteToken(String token);
}

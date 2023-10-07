package net.fileme.service;

import com.baomidou.mybatisplus.extension.service.IService;
import net.fileme.domain.pojo.User;
import net.fileme.enums.EmailTemplateEnum;

public interface UserService extends IService<User> {
    User createUser(User guest);
    void prepareTokenEmail(EmailTemplateEnum templateEnum, String emailReceiver);
    void processToken(String token, boolean doClear);
    String lookUpToken(String token);
}

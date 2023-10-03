package net.fileme.service;

import com.baomidou.mybatisplus.extension.service.IService;
import net.fileme.domain.pojo.User;
import net.fileme.domain.token.BaseToken;

public interface UserService extends IService<User> {

    User createUser(User guest);
    void createAndSendToken(BaseToken config, String emailReceiver);
    void sendVerifyToken(User user);
    String verifyToken(String token);
    void setUserVerified(String email);
    void clearToken(String token);
}

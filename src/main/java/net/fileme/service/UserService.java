package net.fileme.service;

import com.baomidou.mybatisplus.extension.service.IService;
import net.fileme.domain.pojo.User;

public interface UserService extends IService<User> {

    void setUserVerified();
    User createUser(User guest);
    void verifyUser(User user);
}

package net.fileme.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import net.fileme.domain.mapper.UserMapper;
import net.fileme.service.UserService;
import net.fileme.domain.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Override
    public void register(User guest){
        String username = guest.getUsername();
        String pwd = guest.getPwd();
        String email = guest.getEmail();

        String encodedPwd = passwordEncoder.encode(pwd);
        // TODO: 寄驗證信

        User user = new User();
        user.setUsername(username);
        user.setPwd(encodedPwd);
        user.setEmail(email);
        save(user);
    }

    // 更改密碼要輸入舊的, 更改email?
    // , 忘記密碼
    @Override
    public void resetPwd(User user){

    }
//    public boolean validPwd(String pwd){
//        passwordEncoder.matches()
//    }
}

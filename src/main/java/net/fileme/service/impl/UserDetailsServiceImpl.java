package net.fileme.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import net.fileme.domain.MyUserDetails;
import net.fileme.domain.mapper.UserMapper;
import net.fileme.domain.pojo.User;
import net.fileme.enums.ExceptionEnum;
import net.fileme.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Objects;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    private UserMapper userMapper;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        LambdaQueryWrapper<User> lqw = new LambdaQueryWrapper<>();
        lqw.eq(User::getUsername, username);
        User user = userMapper.selectOne(lqw);
        if(Objects.isNull(user)){
            throw new BadRequestException(ExceptionEnum.USER_NOT_EXISTS);
        }
        // TODO: 設定權限
        return new MyUserDetails(user, Arrays.asList("user")); // stored in Authentication principal
    }
}

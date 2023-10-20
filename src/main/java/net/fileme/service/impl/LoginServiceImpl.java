package net.fileme.service.impl;

import net.fileme.domain.MyUserDetails;
import net.fileme.domain.Result;
import net.fileme.domain.dto.UserDto;
import net.fileme.service.LoginService;
import net.fileme.utils.JwtUtil;
import net.fileme.utils.RedisCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;


@Service
public class LoginServiceImpl implements LoginService {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private RedisCache redisCache;
    @Value("${user.session.timeout}")
    private Integer timeout;
    @Value("${user.session.time-unit}")
    private TimeUnit timeUnit;
    @Override
    public Result login(UserDto dto) { // TODO: 可再考慮是否要改user還是loginUser

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword());
        Authentication authentication = authenticationManager.authenticate(authenticationToken);

        // TODO: 這邊好像需要自己抓exception 登入錯誤的話

        MyUserDetails myUserDetails = (MyUserDetails) authentication.getPrincipal();
        String userId = myUserDetails.getUser().getId().toString();

        String jwt = JwtUtil.createJWT(userId);
        redisCache.setObj("login:" + userId, myUserDetails, timeout, timeUnit);

        return Result.success(jwt);
    }
    @Override
    public Result logout(){
        UsernamePasswordAuthenticationToken authentication = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        MyUserDetails myUserDetails = (MyUserDetails) authentication.getPrincipal();
        Long userId = myUserDetails.getUser().getId();
        redisCache.deleteObj("login:" + userId);
        return Result.success();
    }
}

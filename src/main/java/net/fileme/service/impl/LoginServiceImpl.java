package net.fileme.service.impl;

import net.fileme.domain.MyUserDetails;
import net.fileme.domain.Result;
import net.fileme.domain.dto.UserDto;
import net.fileme.service.LoginService;
import net.fileme.utils.JwtUtil;
import net.fileme.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class LoginServiceImpl implements LoginService {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private RedisUtil redisUtil;
    @Override
    public Result login(UserDto dto) { // TODO: 可再考慮是否要改user還是loginUser

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword());
        Authentication authentication = authenticationManager.authenticate(authenticationToken);

        // TODO: 這邊好像需要自己抓exception 登入錯誤的話

        MyUserDetails myUserDetails = (MyUserDetails) authentication.getPrincipal();
        String userId = myUserDetails.getUser().getId().toString();

        String jwt = JwtUtil.createJWT(userId);
        // TODO: 是否需要加timeout
        redisUtil.setObj("login:" + userId, myUserDetails);
        // TODO: 是否需要寫成token: JWT
        return Result.success(jwt);
    }
}

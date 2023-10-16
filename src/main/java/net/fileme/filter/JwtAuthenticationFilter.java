package net.fileme.filter;

import io.jsonwebtoken.Claims;
import net.fileme.domain.MyUserDetails;
import net.fileme.enums.ExceptionEnum;
import net.fileme.exception.UnauthorizedException;
import net.fileme.utils.JwtUtil;
import net.fileme.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;

@Component // need to add filter into SecurityConfig
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Autowired
    private RedisUtil redisUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = request.getHeader("token");

        if(!StringUtils.hasText(token)){
            filterChain.doFilter(request, response);
            return;
        }
        // get userId from JWT token
        Claims claims = JwtUtil.parseJWT(token);
        String userId = claims.getSubject();

        // get userDetails from Redis
        MyUserDetails myUserDetails = redisUtil.getRedisValue("login:" + userId);
        if(Objects.isNull(myUserDetails)) throw new UnauthorizedException(ExceptionEnum.GUEST_NOT_ALLOWED);

        // encapsulate authorities
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(myUserDetails, null, myUserDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        filterChain.doFilter(request, response);
    }
}

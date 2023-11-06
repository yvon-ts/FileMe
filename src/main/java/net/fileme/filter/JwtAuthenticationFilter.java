package net.fileme.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import net.fileme.domain.MyUserDetails;
import net.fileme.enums.ExceptionEnum;
import net.fileme.utils.JwtUtil;
import net.fileme.utils.RedisCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;

@Component // need to add filter into SecurityConfig
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Autowired
    private RedisCache redisCache;
    @Autowired
    @Qualifier("handlerExceptionResolver")
    private HandlerExceptionResolver resolver;

    private Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = request.getHeader("token");

        if(!StringUtils.hasText(token)){
            filterChain.doFilter(request, response);
            return;
        }
        String userId = "";
        try{
            // get userId from JWT token
            Claims claims = JwtUtil.parseJWT(token);
            userId = claims.getSubject();
            request.setAttribute("parsedJwt", userId);
        }catch(SignatureException ex){
            log.error(ex.getClass().toString());
            log.error("token: " + token);
            log.error(ex.getMessage());
            request.setAttribute("errMsg", ExceptionEnum.TOKEN_SIGNATURE_ERROR.getDesc());
            request.getRequestDispatcher("/access-denied").forward(request, response);
            return;
        }catch(ExpiredJwtException ex) {
            log.error(ex.getClass().toString());
            log.error("token: " + token);
            log.error(ex.getMessage());
            request.setAttribute("errMsg", ExceptionEnum.EXPIRED_TOKEN.getDesc());
            request.getRequestDispatcher("/access-denied").forward(request, response);
            return;
        }

        // get userDetails from Redis
        MyUserDetails myUserDetails = redisCache.getRedisValue("login:" + userId);
        if(Objects.isNull(myUserDetails)){
            log.error(ExceptionEnum.GUEST_NOT_ALLOWED.toStringDetails());
            request.setAttribute("errMsg", ExceptionEnum.GUEST_NOT_ALLOWED.getDesc());
            request.getRequestDispatcher("/access-denied").forward(request, response);
        }

        // encapsulate authorities
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(myUserDetails, null, myUserDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        filterChain.doFilter(request, response);
    }
}

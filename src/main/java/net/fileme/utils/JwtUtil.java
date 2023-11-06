package net.fileme.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.JwtBuilder;
import net.fileme.exception.BizException;
import net.fileme.enums.ExceptionEnum;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Objects;
import java.util.Properties;
import java.util.UUID;

public class JwtUtil {

    public static final Long JWT_EXP; // minute
    public static final String JWT_KEY;
    public static final String JWT_ISSUER;

    static{
        Properties prop = new Properties();
        InputStream resource = JwtUtil.class.getClassLoader().getResourceAsStream("credentials.properties");
        try {
            prop.load(resource);
            JWT_EXP = Long.valueOf(prop.getProperty("jwt.exp.default"));
            JWT_KEY = prop.getProperty("jwt.key");
            JWT_ISSUER = prop.getProperty("jwt.issuer");
        } catch (IOException e) {
            throw new BizException(ExceptionEnum.JWT_CONFIG_ERROR);
        }
    }

    public static Key generalKey(){
//        byte[] encodedKey = Base64.getDecoder().decode(JwtUtil.JWT_KEY);
//        SecretKey key = new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES");
        return Keys.hmacShaKeyFor(JWT_KEY.getBytes(StandardCharsets.UTF_8));
    }
    public static String createJWT(String subject){
        return getJwtBuilder(subject, null).compact();
    }
    public static String createJWT(String subject, Long duration){
        return getJwtBuilder(subject, duration).compact();
    }
    private static JwtBuilder getJwtBuilder(String subject, Long duration){
        Key key = generalKey();
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        if(Objects.isNull(duration)){
            duration = JWT_EXP;
        }
        long expMillis = nowMillis + (duration * 60 * 1000); // millisecond
        Date expDate = new Date(expMillis);
        System.out.println(new Date(expMillis - nowMillis));
        return Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setSubject(subject)
                .setIssuer(JWT_ISSUER)
                .setIssuedAt(now) // issue time
                .signWith(key)
                .setExpiration(expDate);
    }

    // --------- JWT parser --------- //

    public static Claims parseJWT(String jwt){
        Key key = generalKey();
        return Jwts.parser()
                .setSigningKey(key)
                .parseClaimsJws(jwt)
                .getBody();
    }
    // --------- testing --------- //
    public static void main(String[] args) throws FileNotFoundException {

        String jwt = createJWT("subject");
        System.out.println(jwt);
//        Claims claims = parseJWT(jwt);
//        String decode = claims.getSubject();
//        System.out.println(decode);
    }

}

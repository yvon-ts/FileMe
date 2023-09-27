package net.fileme.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import io.jsonwebtoken.JwtBuilder;
import net.fileme.exception.BizException;
import net.fileme.utils.enums.ExceptionEnum;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.util.Base64;
import java.util.Date;
import java.util.Properties;
import java.util.UUID;

public class JwtUtil {

    public static final Long JWT_EXP; // millisecond
    public static final String JWT_KEY;
    public static final String JWT_ISSUER;

    static{
        Properties prop = new Properties();
        InputStream resource = JwtUtil.class.getClassLoader().getResourceAsStream("credentials.properties");
        try {
            prop.load(resource);
            JWT_EXP = Long.valueOf(prop.getProperty("jwt.exp")) * 60 * 1000;
            JWT_KEY = prop.getProperty("jwt.key");
            JWT_ISSUER = prop.getProperty("jwt.issuer");
        } catch (IOException e) {
            throw new BizException(ExceptionEnum.CONFIG_ERROR);
        }
    }

    public static SecretKey getSecretKey(){
        byte[] encodedKey = Base64.getDecoder().decode(JwtUtil.JWT_KEY);
        SecretKey key = new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES");
        return key;
    }
    public static String getUUID(){
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        return uuid;
    }

    public static String createJWT(String subject){
        return getJwtBuilder(subject, null, null).compact();
    }
    private static JwtBuilder getJwtBuilder(String subject, Long duration, String uuid){
        SignatureAlgorithm algorithm = SignatureAlgorithm.HS256;
        SecretKey secretKey = getSecretKey();
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        if(duration == null){
            duration = JwtUtil.JWT_EXP;
        }
        if(uuid == null){
            uuid = getUUID();
        }
        long expMillis = nowMillis + duration;
        Date expDate = new Date(expMillis);
        return Jwts.builder()
                .setId(uuid)
                .setSubject(subject)
                .setIssuer(JWT_ISSUER)
                .setIssuedAt(now) // issue time
                .signWith(algorithm, secretKey)
                .setExpiration(expDate);
    }

    // --------- JWT parser --------- //

    public static Claims parseJWT(String jwt){
        SecretKey secretKey = getSecretKey();
        return Jwts.parser()
                .setSigningKey(secretKey)
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

package net.fileme.utils;

import java.security.SecureRandom;

public class RandomUtil {
    private static final int TOKEN_LEN = 32;
    private static final String CHARS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    public static String createToken(){
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();

        for(int i = 0; i < TOKEN_LEN; i++){
            int index = random.nextInt(CHARS.length());
            sb.append(CHARS.charAt(index));
        }
        return sb.toString();
    }
}

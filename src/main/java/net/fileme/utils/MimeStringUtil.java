package net.fileme.utils;

import net.fileme.enums.MimeEnum;

public class MimeStringUtil {
    public static void main(String[] args){
        StringBuilder sb = new StringBuilder();
        for (MimeEnum type : MimeEnum.values()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(type.mimeType);
        }
        String fileTypeString = sb.toString();
        System.out.println(fileTypeString);
    }
}

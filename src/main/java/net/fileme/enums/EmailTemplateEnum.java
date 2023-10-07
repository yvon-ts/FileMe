package net.fileme.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EmailTemplateEnum {
    DEFAULT(null, null, null,
            "response", "驗證成功，請重新登入")

    , SIGN_UP(1, "response", "請至您的信箱進行驗證"
            ,"response", "驗證成功，現在就立即登入體驗 FileMe 的貼心服務！")

    , RESET(2, "response", "請至您的信箱進行密碼重置"
            ,"response", "密碼重置成功，請重新登入")

    , SET_EMAIL(3, "response", "請至您的信箱進行驗證"
            ,"response", "信箱變更成功，請重新登入")

    , SET_PWD(4, "response", "密碼已重置，請重新登入"
            ,"response", "密碼變更成功，請重新登入");

    private Integer mapping;
    private String asyncView;
    private String asyncViewText;
    private String view;
    private String viewText;
}

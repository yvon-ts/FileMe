package net.fileme.exception;

import lombok.Getter;

@Getter
public enum ExceptionEnum {

    SUCCESS(200, "success")
    // 一般異常
    , EMPTY_PARAM(600, "參數為空值")
    // 檔案異常
    , DUPLICATED(800, "該檔案或目錄已存在")
    , DUPLICATED_DB(801, "DB已有資料")
    , DUPLICATED_SVR(802, "Server已有資料")
    , UPLOAD_FAIL(810, "檔案寫入異常")
    , UPLOAD_DB_FAIL(811, "檔案寫入DB異常")
    , UPLOAD_SVR_FAIL(812, "檔案寫入Server異常")
    , FILE_ERROR(820,"檔案異常")
    , FILE_NAME_ERROR(821, "檔名異常")
    , FILE_SIZE_ERROR(822,"檔案大小異常");

    private final Integer code;
    private final String desc;

    ExceptionEnum(Integer code, String desc){
        this.code = code;
        this.desc = desc;
    }
}

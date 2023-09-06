package net.fileme.exception;

import lombok.Getter;

@Getter
public enum ExceptionEnum {

    SUCCESS(200, "success")
    // 一般異常
    , PARAM_EMPTY(600, "參數為空值")
    , PARAM_ERROR(610, "參數異常")
    , VIOLATE_KEY(620, "鍵值衝突，請檢查ID是否存在")
    , UPDATE_DB_FAIL(630, "操作DB失敗，請檢查ID是否存在")
    // 會員異常
    , USER_NOT_EXISTS(700, "會員不存在")
    // 檔案異常
    , DUPLICATED(800, "該檔案或目錄已存在")
    , DUPLICATED_DB(801, "DB已有資料")
    , DUPLICATED_SVR(802, "Server已有資料")
    , UPLOAD_FAIL(810, "檔案寫入異常")
    , UPLOAD_DB_FAIL(811, "檔案寫入DB異常")
    , UPLOAD_SVR_FAIL(812, "檔案寫入Server異常")
    , FILE_ERROR(820,"檔案異常")
    , FILE_NAME_ERROR(821, "檔名異常")
    , FILE_SIZE_ERROR(822,"檔案大小異常")
    , FOLDER_ERROR(830, "目錄異常")
    , FOLDER_NAME_ERROR(831, "目錄名稱異常")
    , FOLDER_SIZE_ERROR(832, "目錄大小異常")
    , FOLDER_DELETE_FAIL(840, "目錄刪除失敗")
    , FOLDER_NOT_EMPTY(841,"目錄底下尚有資料，請清空後再刪除");

    private final Integer code;
    private final String desc;

    ExceptionEnum(Integer code, String desc){
        this.code = code;
        this.desc = desc;
    }
}

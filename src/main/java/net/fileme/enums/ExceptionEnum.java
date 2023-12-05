package net.fileme.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
/**
 * should upgrade and separate msg for admin/client
 * 前端：
 * 1=可直接顯示
 * 2=顯示替代文字
 * 9=僅後端使用
 *
 * 後端：
 * 0=前端異常,框架validation
 * 1=自定義method validation
 * 2=後端邏輯
 * 3=DB
 * 4=OS
 * 5=Redis
 *
 * 種類：
 * 0=general
 * 1=user
 * 2=folder
 * 3=file
 * 4=auth/token
 * 5=email
 */
public enum ExceptionEnum {

    SUCCESS(200, "success")
    
    , LOGIN_ERROR(10010, "帳號或密碼錯誤")
    , WRONG_PWD(10011, "密碼錯誤")
    , GUEST_NOT_ALLOWED(10020, "不接受訪客操作")
    , PRE_AUTH_FAIL(10030, "權限不足")

    , USERNAME_REGEX_ERROR(10110, "帳號格式不符合規定")
    , PWD_REGEX_ERROR(10111, "密碼格式不符合規定")
    , USER_EMAIL_REGEX_ERROR(10112, "信箱格式不符合規定")

    , FOLDER_NAME_REGEX_ERROR(10210, "目錄名稱不符合規定")
    
    , FILE_NAME_REGEX_ERROR(10310, "檔名不符合規定")
    , FILE_OVER_SIZE(10320,"檔案太大")

    , EXISTING_USERNAME(11110, "該帳號已被使用")
    , EXISTING_EMAIL(11111, "該信箱已被使用")

    , PREVIEW_NOT_ALLOWED(11310, "該檔案不支援預覽")
    , FILE_TYPE_NOT_ALLOWED(11320, "不支援的檔案格式")
    
    , TOKEN_SIGNATURE_ERROR(11410, "金鑰異常，請重新登入")
    , EXPIRED_TOKEN(11411, "金鑰過期，請重新登入")
    
    , WRONG_TOKEN_VERSION(11510, "驗證失敗，請查看最新信件或聯繫客服")

    , EMPTY_FOLDER(13010, "暫無資料")
    , NO_SUCH_DATA(13020, "查無資料")
    , NO_SUCH_USER(13110, "查無此使用者")

    , NO_SUCH_TOKEN(15410, "驗證失敗，請查看最新信件或聯繫客服")

    // ----------------------------------------------------------- //

    , METHOD_NOT_ALLOWED(20010, "請求方式異常")
    , PARAM_ERROR(20011, "請求參數異常")
    
    , NOT_SAME_PARENT(21210, "操作非同層參數異常")

    , FILE_NAME_ERROR(22310, "檔案名稱異常")
    , FILE_TYPE_ERROR(22311,"檔案格式異常")

    , UNIQUE_CONFLICT(23010, "DB已有資料")
    , VIOLATE_KEY(23011, "DB鍵值衝突")
    , HANDLE_DUPLICATED_NEW_FILE_FAIL(23012, "衝突檔名處理失敗(新增檔案)")
    
    , SIGN_UP_FAIL(23110, "註冊失敗")
    , CHANGE_PWD_FAIL(23111, "變更信箱失敗")
    , CHANGE_EMAIL_FAIL(23112, "變更信箱失敗")
    , PWD_RESET_FAIL(23113, "重置密碼失敗")
    , USER_STATE_ERROR(23114, "會員狀態異常")

    , SET_FOLDER_ACCESS_FAIL(23210, "目錄權限變更失敗")
    , FOLDER_RENAME_FAIL(23211, "目錄名稱變更失敗")
    , FOLDER_RELOCATE_FAIL(23212, "目錄移動失敗")
    , CREATE_FOLDER_TRASH_FAIL(23213, "新增目錄垃圾失敗")
    , FOLDER_RECOVER_FAIL(23214, "目錄復原失敗")
    , DELETE_FOLDER_TRASH_FAIL(23215, "刪除目錄垃圾失敗")
    , DELETE_FOLDER_FAIL(23216, "目錄刪除(DB)失敗")
    , NESTED_FOLDER(23217, "目錄結構異常")

    , SET_FILE_ACCESS_FAIL(23310, "檔案權限變更失敗")
    , FILE_RENAME_FAIL(23311, "檔案名稱變更失敗")
    , FILE_RELOCATE_FAIL(23312, "檔案移動失敗")
    , CREATE_FILE_TRASH_FAIL(23313, "新增檔案垃圾失敗")
    , FILE_RECOVER_FAIL(23314, "檔案復原失敗")
    , DELETE_FILE_TRASH_FAIL(23315, "刪除檔案垃圾失敗")
    , DELETE_REMOVE_LIST_FAIL(23316, "檔案刪除(DB)失敗")
    , CREATE_REMOVE_LIST_FAIL(23317, "新增remove list失敗")
    , REMOVE_REMOVE_LIST_FAIL(23318, "刪除remove list資料(DB)失敗")
    
    , UPLOAD_SVR_FAIL(24310, "檔案寫入Server異常")
    , FILE_IO_ERROR(24311, "檔案I/O異常")
    , FILE_EXISTS_ON_SERVER(24320, "Server已有重複檔案")

    , REDIS_DEL_ERROR(25410, "Redis刪除異常")

    , JWT_CONFIG_ERROR(92410, "JWT組態異常")
    , SYS_EMAIL_ERROR(92510, "系統發信異常");


    private final Integer code;
    private final String desc;

    public String toStringDetails() {
        return toString() + ": 錯誤代碼 " + getCode() + ", " + getDesc();
    }
}

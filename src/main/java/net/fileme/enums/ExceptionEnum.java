package net.fileme.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ExceptionEnum {

    SUCCESS(200, "success")
    // 一般異常
    , PARAM_EMPTY(600, "參數為空值")
    , PARAM_ERROR(610, "參數異常")
    , VIOLATE_KEY(620, "鍵值衝突，請檢查ID是否存在")
    , UPDATE_DB_FAIL(630, "操作DB失敗，請檢查ID是否存在")
    , CONFIG_ERROR(640, "組態異常")
    , UPDATE_FAIL(650, "更新失敗")
    , LOGIN_TIMEOUT(660, "請重新登入")
    , GUEST_NOT_ALLOWED(690, "不接受訪客操作，請先登入")
    , PRE_AUTH_FAIL(691, "權限不足")
    , SYS_EMAIL_ERROR(699, "系統發信異常")
    , METHOD_NOT_ALLOWED(661, "請求方式異常")
    // 會員異常
    , USER_NOT_EXISTS(700, "帳號或密碼錯誤")
    , USERNAME_ERROR(701, "帳號格式不符合規定")
    , EXISTING_USERNAME(702, "該帳號已被使用")
    , EXISTING_EMAIL(703, "該信箱已被使用")
    , PWD_ERROR(702, "密碼格式不符合規定")
    , WRONG_PWD(704, "密碼錯誤")
    , USER_EMAIL_ERROR(703, "信箱格式不符合規定")
    , USER_STATE_ERROR(710, "會員狀態異常")
    , INVALID_TOKEN(780, "驗證失敗，請聯繫客服")
    , EXPIRED_TOKEN(781, "操作逾時，請重新登入")
    , TOKEN_DEL_ERROR(781, "金鑰刪除異常")
    , REGISTER_FAIL(799, "註冊異常")
    // 重複異常
    , DUPLICATED(800, "該檔案或目錄已存在")
    , DUPLICATED_USERNAME(801, "帳號已被使用")
    , DUPLICATED_EMAIL(802, "信箱已被使用")
    , DUPLICATED_DB(803, "DB已有資料")
    , DUPLICATED_SVR(804, "Server已有資料")

    // 檔案異常
    , UPLOAD_FAIL(810, "檔案寫入異常")
    , UPLOAD_DB_FAIL(811, "檔案寫入DB異常")
    , UPLOAD_SVR_FAIL(812, "檔案寫入Server異常")
    , FILE_ERROR(820,"檔案異常")
    , FILE_NAME_ERROR(821, "檔案名稱不符合規定")
    , DATA_NAME_ERROR(888, "名稱不符合規定")
    , FILE_SIZE_ERROR(822,"檔案大小異常")
    , FILE_TYPE_ERROR(823,"檔案格式異常")
    , FILE_NOT_EXISTS(824, "檔案不存在，操作失敗")
    , PREVIEW_NOT_ALLOWED(830, "該檔案不支援預覽")
    // 目錄異常
    , FOLDER_ERROR(830, "目錄異常")
    , FOLDER_NAME_ERROR(831, "目錄名稱不符合規定")
    , FOLDER_SIZE_ERROR(832, "目錄大小異常")
    , FOLDER_NOT_EXISTS(833, "目錄不存在，操作失敗")
    , DATA_DELETE_FAIL(840, "資料刪除失敗")
    , DATA_NOT_EXISTS(841,"目標資料不存在，操作失敗")
    , FOLDER_NOT_EMPTY(842,"目錄底下尚有資料，請清空後再刪除")
    , NO_SUCH_DATA(843, "查無資料")
    , NESTED_FOLDER(844, "目錄結構異常")
    , NOT_SAME_PARENT(845, "操作非同層參數異常");

    private final Integer code;
    private final String desc;

    public String toStringDetails() {
        return toString() + ": 錯誤代碼 " + getCode() + ", " + getDesc();
    }
}

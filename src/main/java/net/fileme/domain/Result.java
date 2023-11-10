package net.fileme.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import net.fileme.enums.ExceptionEnum;

import java.io.Serializable;

@Schema(description = "API回傳內容")
@Data
public class Result<T> implements Serializable {
    @Schema(description = "回應代碼",
            example = "200")
    private Integer code;

    @Schema(description = "回應訊息",
            example = "範例訊息")
    private String msg;

    @Schema(description = "回傳資料")
    private T data;

    private Result(Integer code, String msg, T data){
        this.code = code;
        this.msg = msg;
        this.data = data;
    }
    private Result(Integer code, String msg){
        this.code = code;
        this.msg = msg;
    }

    public static<T> Result<T> success(T data){
        return new Result<T>(ExceptionEnum.SUCCESS.getCode(), ExceptionEnum.SUCCESS.getDesc(), data);
    }

    public static Result success(){
        return success(null);
    }

    public static Result error(ExceptionEnum error){
        return new Result(error.getCode(), error.getDesc());
    }
    public static<T> Result<T> error(ExceptionEnum error, T data){
        return new Result(error.getCode(), error.getDesc(), data);
    }
}

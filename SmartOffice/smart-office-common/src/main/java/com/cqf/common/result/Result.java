package com.cqf.common.result;

import com.cqf.common.enums.ResultCode;
import lombok.Data;

import java.io.Serializable;
@Data
public class Result <T> implements Serializable {
    private Integer code;
    private String msg;
    private T data;
    private Long timestamp;
    public static <T> Result<T> success(T data){
        Result<T> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMsg("成功");
        result.setData(data);
        result.setTimestamp(System.currentTimeMillis());
        return result;
    }
    public static <T> Result<T> error(String msg) {
        return error(ResultCode.FAILED.getCode(), msg);
    }

    public static <T> Result<T> error(Integer code, String msg) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMsg(msg);
        result.setTimestamp(System.currentTimeMillis());
        return result;
    }

    public static <T> Result<T> error(Integer code, String msg, T data) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMsg(msg);
        result.setData(data);
        result.setTimestamp(System.currentTimeMillis());
        return result;
    }
}

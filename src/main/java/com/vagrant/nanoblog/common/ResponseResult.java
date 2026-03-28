package com.vagrant.nanoblog.common;

public class ResponseResult<T> {

    private Integer code;
    private String msg;
    private T data;

    public ResponseResult() {}

    public ResponseResult(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    // 成功返回（带数据）
    public static <T> ResponseResult<T> okResult(T data) {
        return new ResponseResult<>(200, "success", data);
    }

    // 成功返回（无数据）
    public static <T> ResponseResult<T> okResult() {
        return new ResponseResult<>(200, "success", null);
    }

    // 失败返回
    public static <T> ResponseResult<T> errorResult(int code, String msg) {
        ResponseResult<T> result = new ResponseResult<>();
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }


    public Integer getCode() { return code; }
    public void setCode(Integer code) { this.code = code; }

    public String getMsg() { return msg; }
    public void setMsg(String msg) { this.msg = msg; }

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
}
package edu.hit.fmpmm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result {
    private Boolean success;  // 是否成功
    private String errorMsg;  // 错误信息
    private Object data;  // 返回的数据

    public static Result success() {
        return new Result(true, null, null);
    }

    public static Result success(Object data) {
        return new Result(true, null, data);
    }

    public static Result fail(String errorMsg) {
        return new Result(false, errorMsg, null);
    }

}

package cn.mrcode.dtdemo.a.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class R<T> {
    private Integer code;    // 状态码
    private String message;  // 消息
    private T data;          // 数据

    public static <T> R<T> success(T data) {
        return new R<>(200, "操作成功", data);
    }
}

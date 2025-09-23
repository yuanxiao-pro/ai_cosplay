package edu.cust.secad.model.base;

import lombok.Data;

/**
 * @ClassDescription: 自定义全局异常类
 * @Author: Nvgu
 * @Created: 2024/10/12 18:58
 * @Updated: 2024/10/12 18:58
 */
@Data
public class HuaTuoException extends RuntimeException{
    private Integer code;

    private String message;

    /**
     * 通过状态码和错误消息创建异常对象
     * @param code
     * @param message
     */
    public HuaTuoException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    /**
     * 接收枚举类型对象
     * @param resultCodeEnum
     */
    public HuaTuoException(ResultCodeEnum resultCodeEnum) {
        super(resultCodeEnum.getMessage());
        this.code = resultCodeEnum.getCode();
        this.message = resultCodeEnum.getMessage();
    }

    @Override
    public String toString() {
        return "HuaTuoException{" +
                "code=" + code +
                ", message=" + this.getMessage() +
                '}';
    }
}
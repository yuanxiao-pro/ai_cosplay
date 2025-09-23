
package edu.cust.secad.model.hanlder;
import com.fasterxml.jackson.core.JsonProcessingException;
import edu.cust.secad.model.base.HuaTuoException;
import edu.cust.secad.model.base.Result;
import edu.cust.secad.model.base.ResultCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 全局异常处理类
 *
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Result error(Exception e){
        e.printStackTrace();
        return Result.fail();
    }

    /**
     * 自定义异常处理方法
     * @param e
     * @return
     */
    @ExceptionHandler(HuaTuoException.class)
    @ResponseBody
    public Result error(HuaTuoException e){
        e.printStackTrace();
        return Result.build(null, e.getCode(), e.getMessage());
    }

//    @ExceptionHandler(DecodeException.class)
//    @ResponseBody
//    public Result error(DecodeException e){
//        e.printStackTrace();
//        return Result.build(null,e.get,  e.getMessage());
//    }

    @ExceptionHandler({IllegalArgumentException.class})
    @ResponseBody
    public Result llegalArgumentException(Exception e) {
        e.printStackTrace();
        log.error("触发异常拦截: " + e.getMessage(), e);
        return Result.build(null, ResultCodeEnum.ARGUMENT_VALID_ERROR);
    }

//    /**
//     * spring security异常
//     * @param e
//     * @return
//     */
//    @ExceptionHandler(AccessDeniedException.class)
//    @ResponseBody
//    public Result error(AccessDeniedException e) throws AccessDeniedException {
//        return Result.build(null, ResultCodeEnum.PERMISSION);
//    }

    @ExceptionHandler(value = BindException.class)
    @ResponseBody
    public Result error(BindException exception) {
        BindingResult result = exception.getBindingResult();
        Map<String, Object> errorMap = new HashMap<>();
        List<FieldError> fieldErrors = result.getFieldErrors();
        fieldErrors.forEach(error -> {
            log.error("field: " + error.getField() + ", msg:" + error.getDefaultMessage());
            errorMap.put(error.getField(), error.getDefaultMessage());
        });
        return Result.build(errorMap, ResultCodeEnum.ARGUMENT_VALID_ERROR);
    }
    @ExceptionHandler(JsonProcessingException.class)
    @ResponseBody
    public Result error(JsonProcessingException e) {
        log.error("JSON 处理异常: {}", e.getMessage());
        return Result.build(null, ResultCodeEnum.FAIL.getCode(), "JSON 格式错误: " + e.getOriginalMessage());
    }
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    @ResponseBody
    public Result error(MethodArgumentNotValidException exception) {
        BindingResult result = exception.getBindingResult();
        Map<String, Object> errorMap = new HashMap<>();
        List<FieldError> fieldErrors = result.getFieldErrors();
        fieldErrors.forEach(error -> {
            log.error("field: " + error.getField() + ", msg:" + error.getDefaultMessage());
            errorMap.put(error.getField(), error.getDefaultMessage());
        });
        return Result.build(errorMap, ResultCodeEnum.ARGUMENT_VALID_ERROR);
    }


}


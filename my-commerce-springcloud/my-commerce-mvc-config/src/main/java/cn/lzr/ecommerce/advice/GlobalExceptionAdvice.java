package cn.lzr.ecommerce.advice;

import cn.lzr.ecommerce.vo.CommonResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionAdvice {

    // 这个advice用于所有异常类型
    @ExceptionHandler(value = Exception.class)
    public CommonResponse<String> handlerCommerceException(Exception ex) {
        CommonResponse<String> response = new CommonResponse<>(-1, "business error");
        response.setData(ex.getMessage());
        log.error("commerce service has error: [{}]", ex.getMessage(), ex);
        // 最终这个Controller返回还是CommonResponse类型，保证了前端的数据格式统一
        return response;
    }
}

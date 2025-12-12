package com.problemio.global.exception;

import com.problemio.global.common.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        ErrorCode errorCode = e.getErrorCode();
        // 인증/인가 관련 에러는 적절한 HTTP 상태 코드로 내려준다.
        return switch (errorCode) {
            case LOGIN_REQUIRED -> ResponseEntity
                    .status(401)
                    .body(ApiResponse.fail(errorCode.getCode(), errorCode.getMessage()));
            case ACCESS_DENIED -> ResponseEntity
                    .status(403)
                    .body(ApiResponse.fail(errorCode.getCode(), errorCode.getMessage()));
            default -> ResponseEntity
                    .badRequest()
                    .body(ApiResponse.fail(errorCode.getCode(), errorCode.getMessage()));
        };
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        // TODO: 로그 처리
        return ResponseEntity
                .internalServerError()
                .body(ApiResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR.getCode(),
                        ErrorCode.INTERNAL_SERVER_ERROR.getMessage()));
    }
}

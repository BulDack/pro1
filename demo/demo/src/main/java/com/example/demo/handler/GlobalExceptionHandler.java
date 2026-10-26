package com.example.demo.handler;


import com.example.demo.Exception.CustomException;
import com.example.demo.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

//전역 예외 처리기(다른데에서 던져진 예외들을 이놈이 가로챔)
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    //1.비즈니스 로직 예외 처리
//    @ExceptionHandler(IllegalArgumentException.class)
//    public ResponseEntity<ApiResponse<Void>>handleIllegalArgumentException(IllegalArgumentException e){
//        //여기서 ApiResponse.fail()를 호출해서 응답을 만듬
//        return ResponseEntity
//                .status(HttpStatus.BAD_REQUEST)
//                .body(ApiResponse.fail(e.getMessage()));
//    }

    //2.@valid 유효성 검사 실패 처리(예: 이메일 형식오류,필수값 누락)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>>handleValidationException(MethodArgumentNotValidException e){
        String errorMessage=e.getBindingResult().getAllErrors().get(0).getDefaultMessage();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(errorMessage));
    }

    //3.커스텀한 예외 처리(위에 두개는 결국 다 이걸로 통합가능)
    @ExceptionHandler
    public ResponseEntity<ApiResponse<Void>>handleCustomException(CustomException e){
        // 1. 백엔드 서버에는 경고 로그 기록
        log.warn("[CustomException] Code: {}, Message: {}", e.getErrorCode().getCode(), e.getMessage());

        // 2. 프론트엔드에는 약속된 JSON 형태의 에러 응답 전달
        return ResponseEntity
                .status(e.getErrorCode().getHttpStatus())
                .body(ApiResponse.fail(e.getErrorCode().getMessage()));
    }


    //4.. 기타서버 내부 알 수 없는 에러(500 server Error)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handlAllException(Exception e) {
        // 예외 메시지와 발생 위치를 서버 콘솔에 기록한다.
        log.error("처리되지 않은 서버 오류", e);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail("서버 내부 오류가 발생했습니다."));
    }


}

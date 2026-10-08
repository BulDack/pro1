package com.example.demo.Exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Common (공통 에러)
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "잘못된 입력값입니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C002", "지원하지 않는 HTTP 메서드입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C003", "서버 내부 오류가 발생했습니다."),

    // Auth & JWT (인증/인가 관련 에러)
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "A001", "유효하지 않거나 서명이 일치하지 않는 Refresh Token입니다."),
    EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "A002", "만료된 Refresh Token입니다. 다시 로그인 해주세요."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "A003", "Refresh Token을 찾을 수 없습니다."),
    TOKEN_TAMPERED(HttpStatus.UNAUTHORIZED, "A004", "토큰이 변조되었거나 탈취 시도가 감지되었습니다."),
    UNAUTHORIZED_ACCESS(HttpStatus.FORBIDDEN, "A005", "접근 권한이 없습니다."),

    // User (사용자 관련 에러 예시)
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "존재하지 않는 회원입니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "U002", "이미 존재하는 이메일입니다.");

    private final HttpStatus httpStatus; // HTTP 상태 코드 (400, 401, 404 등)
    private final String code;           // 비즈니스 전용 에러 코드
    private final String message;        // 프론트엔드로 전달할 에러 메시지
}

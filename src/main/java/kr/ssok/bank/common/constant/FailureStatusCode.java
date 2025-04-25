package kr.ssok.bank.common.constant;

import kr.ssok.bank.common.response.BaseCode;
import kr.ssok.bank.common.response.BaseResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum FailureStatusCode implements BaseCode {

    // 가장 일반적인 응답
    _INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "서버 에러, 관리자에게 문의 바랍니다."),
    _BAD_REQUEST(HttpStatus.BAD_REQUEST,"COMMON400","잘못된 요청입니다."),
    _UNAUTHORIZED(HttpStatus.UNAUTHORIZED,"COMMON401","인증이 필요합니다."),
    _FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON403", "금지된 요청입니다."),

    // 유저 관련 에러
    USER_NOT_FOUND(HttpStatus.BAD_REQUEST, "USER4001", "사용자가 없습니다."),
    USER_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "USER4002", "사용자가 이미 존재합니다."),
    USER_CREATION_FAILED(HttpStatus.BAD_REQUEST, "USER4003", "사용자 생성에 실패했습니다."),
    USER_TYPE_ERROR(HttpStatus.BAD_REQUEST, "USER4004", "사용자 유형이 유효하지 않습니다."),

    // 로그인 관련 에러
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "AUTH4002", "아이디 또는 비밀번호를 확인해주세요."),
    INVALID_TOKEN(HttpStatus.BAD_REQUEST,"AUTH4001", "잘못된 토큰입니다."),

    ACCOUNT_NOT_FOUND(HttpStatus.BAD_REQUEST, "ACNT4001", "해당 계좌 번호는 존재하지 않습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public BaseResponseDTO getReason() {
        return BaseResponseDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(false)
                .build();
    }

    @Override
    public BaseResponseDTO getReasonHttpStatus() {
        return BaseResponseDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(false)
                .httpStatus(httpStatus)
                .build()
                ;
    }
}
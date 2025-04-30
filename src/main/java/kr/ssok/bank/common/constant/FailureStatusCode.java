package kr.ssok.bank.common.constant;

import kr.ssok.bank.common.response.BaseCode;
import kr.ssok.bank.common.response.BaseResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum FailureStatusCode implements BaseCode {

    // 일반 응답
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
    INVALID_TOKEN(HttpStatus.BAD_REQUEST,"AUTH4001", "잘못된 토큰입니다."),
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "AUTH4002", "아이디 또는 비밀번호를 확인해주세요."),

    //계좌 관련 에러
    ACCOUNT_NOT_FOUND(HttpStatus.BAD_REQUEST, "ACCOUNT4001", "해당 계좌는 존재하지 않습니다."),
    ACCOUNT_NUMBER_NOT_FOUND(HttpStatus.BAD_REQUEST, "ACCOUNT4002", "해당 계좌 번호는 존재하지 않습니다."),
    ACCOUNT_OWNER_CHECK_FAILED(HttpStatus.BAD_REQUEST, "ACCOUNT4003", "해당 계좌 번호에 일치하는 예금주가 없습니다."),
    ACCOUNT_VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "ACCOUNT4004", "예금주와 계좌 정보가 일치하지 않습니다."),
    ACCOUNT_READ_FAILED(HttpStatus.BAD_REQUEST, "ACCOUNT4005", "계좌 조회에 실패하였습니다."),
    ACCOUNT_DORMANT_FAILED(HttpStatus.BAD_REQUEST, "ACCOUNT4006", "휴면 계좌 여부 조회에 실패했습니다."),
    ACCOUNT_CREATE_FAILED(HttpStatus.BAD_REQUEST, "ACCOUNT4007", "계좌 생성에 실패하였습니다."),
    ACCOUNT_BALANCE_FAILED(HttpStatus.BAD_REQUEST, "ACCOUNT4008", "계좌 잔액 조회에 실패하였습니다."),
    ACCOUNT_WITHDRAW_LIMIT_REACHED(HttpStatus.BAD_REQUEST, "ACCOUNT4009", "해당 계좌의 출금 한도에 도달하였습니다."),
    ACCOUNT_HISTORY_FAILED(HttpStatus.BAD_REQUEST, "ACCOUNT4010", "거래 내역 조회에 실패하였습니다."),

    //거래 관련 에러
    TRANSACTION_NOT_EXISTS(HttpStatus.BAD_REQUEST, "TRANSACTION4002", "거래 내역이 존재하지 않습니다."),
    TRANSACTION_CREATE_FAILED(HttpStatus.BAD_REQUEST, "TRANSACTION4003", "거래 내역 생성에 실패하였습니다."),

    //송금 관련 에러
    TRANSFER_FAILED(HttpStatus.BAD_REQUEST, "TRANSFER4001", "송금에 실패하였습니다."),
    TRANSFER_DEPOSIT_FAILED(HttpStatus.BAD_REQUEST, "TRANSFER4002", "입금 이체에 실패하였습니다."),
    TRANSFER_WITHDRAW_FAILED(HttpStatus.BAD_REQUEST, "TRANSFER4003", "출금 이체에 실패하였습니다."),
    TRANSFER_NO_BALANCE(HttpStatus.BAD_REQUEST, "TRANSFER4004", "해당 계좌는 잔액이 부족합니다."),

    //상품 관련 에러
    GOOD_READ_FAILED(HttpStatus.BAD_REQUEST, "GOOD4001", "상품 목록이 존재하지 않습니다.");

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
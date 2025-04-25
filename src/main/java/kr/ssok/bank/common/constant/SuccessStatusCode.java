package kr.ssok.bank.common.constant;

import kr.ssok.bank.common.response.BaseCode;
import kr.ssok.bank.common.response.BaseResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SuccessStatusCode implements BaseCode {

    // 일반 응답
    _OK(HttpStatus.OK, "COMMON200", "성공적으로 처리되었습니다."),

    // 송금 관련
    TRANSFER_OK(HttpStatus.OK, "TRANSFER2001", "송금에 성공하였습니다."),
    TRANSFER_WITHDRAW_OK(HttpStatus.OK, "TRANSFER2002", "출금 이체에 성공하였습니다."),
    TRANSFER_DEPOSIT_OK(HttpStatus.OK, "TRANSFER2003", "입금 이체에 성공하였습니다."),
    TRANSFER_AVAILABLE(HttpStatus.OK, "ACCCOUNT2008", "해당 계좌는 송금 처리 가능합니다."),

    //유저 관련
    USER_FOUND_OK(HttpStatus.OK, "USER2001", "사용자를 찾았습니다."),
    USER_CREATION_OK(HttpStatus.OK, "USER2002", "사용자 생성에 성공하였습니다."),

    // 계좌 관련
    ACCOUNT_READ_OK(HttpStatus.OK, "ACCOUNT2001", "계좌 조회에 성공하였습니다."),
    ACCOUNT_DORMANT_OK(HttpStatus.OK, "ACCOUNT2002", "휴면 계좌 여부 조회에 성공했습니다."),
    ACCOUNT_CREATE_OK(HttpStatus.OK, "ACCOUNT2003","계좌 생성이 완료되었습니다."),
    ACCOUNT_BALANCE_OK(HttpStatus.OK, "ACCOUNT2004","계좌 잔액 조회에 성공하였습니다."),
    ACCOUNT_VALIDATION_OK(HttpStatus.OK, "ACCCOUNT2005", "예금주와 계좌 정보가 일치합니다."),
    ACCOUNT_OWNER_CHECK_OK(HttpStatus.OK, "ACCCOUNT2006", "해당 계좌번호의 소유자(예금주)를 확인하였습니다."),
    ACCOUNT_NUMBER_FOUND_OK(HttpStatus.OK, "ACCCOUNT2007", "해당 계좌 번호는 존재합니다."),
    ACCOUNT_HISTORY_OK(HttpStatus.OK, "ACCCOUNT2008", "거래 내역이 조회 되었습니다.");


    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public BaseResponseDTO getReason() {
        return BaseResponseDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(true)
                .build();
    }

    @Override
    public BaseResponseDTO getReasonHttpStatus() {
        return BaseResponseDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(true)
                .httpStatus(httpStatus)
                .build()
                ;
    }
}
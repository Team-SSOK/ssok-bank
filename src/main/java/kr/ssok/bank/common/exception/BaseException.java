package kr.ssok.bank.common.exception;

import kr.ssok.bank.common.response.code.status.ErrorStatus;
import lombok.Getter;

@Getter
public class BaseException extends RuntimeException {

    private ErrorStatus status;

    public BaseException(ErrorStatus status) {
        super(status.getMessage());
        this.status = status;
    }
}
package kr.ssok.bank.common.exception;

import kr.ssok.bank.common.response.code.status.ErrorStatusCode;
import lombok.Getter;

@Getter
public class BaseException extends RuntimeException {

    private ErrorStatusCode status;

    public BaseException(ErrorStatusCode status) {
        super(status.getMessage());
        this.status = status;
    }
}
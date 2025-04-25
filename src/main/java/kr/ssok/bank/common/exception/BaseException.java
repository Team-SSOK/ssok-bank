package kr.ssok.bank.common.exception;

import kr.ssok.bank.common.constant.FailureStatusCode;
import lombok.Getter;

@Getter
public class BaseException extends RuntimeException {

    private FailureStatusCode status;

    public BaseException(FailureStatusCode status) {
        super(status.getMessage());
        this.status = status;
    }
}
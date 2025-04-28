package kr.ssok.bank.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TransferTypeCode {
    // 입금
    DEPOSIT(1,"DEPOSIT"),
    // 출금
    WITHDRAW(2,"WITHDRAW");

    private final int idx;
    private final String value;
}

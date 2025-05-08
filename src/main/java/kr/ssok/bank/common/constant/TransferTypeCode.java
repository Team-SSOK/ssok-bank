package kr.ssok.bank.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TransferTypeCode {
    // 입금
    DEPOSIT(0,"DEPOSIT"),
    // 출금
    WITHDRAW(1,"WITHDRAW"),
    // 이자
    INTEREST(2, "INTEREST"),
    // 보상
    COMPENSATE(3,"COMPENSATE");

    private final int idx;
    private final String value;
}

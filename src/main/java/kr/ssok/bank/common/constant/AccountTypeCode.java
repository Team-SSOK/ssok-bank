package kr.ssok.bank.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AccountTypeCode {
    // 예금
    DEPOSIT(1, "예금"),

    // 적금
    SAVINGS(2, "적금"),

    // 청약
    SUBSCRIPTION(3, "청약");

    private final int idx;
    private final String value;

    public static AccountTypeCode valueOf(int input) {
        switch (input) {
            case 1: return AccountTypeCode.DEPOSIT;
            case 2: return AccountTypeCode.SAVINGS;
            case 3: return AccountTypeCode.SUBSCRIPTION;
            default: return null;
        }
    }


}

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
}

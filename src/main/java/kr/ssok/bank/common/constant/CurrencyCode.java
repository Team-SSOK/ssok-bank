package kr.ssok.bank.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CurrencyCode {
    // 원화
    WON(0,"WON"),
    // 달러화
    DOLLAR(1,"DOLLAR");

    private final int idx;
    private final String value;
}

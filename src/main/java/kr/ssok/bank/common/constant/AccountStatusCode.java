package kr.ssok.bank.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AccountStatusCode {
    // 휴면
    DORMANT(0, "휴면"),

    // 활성
    ACTIVE(1, "활성");

    private final int idx;
    private final String value;
}

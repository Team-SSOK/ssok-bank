package kr.ssok.bank.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BankCode {
    SSOK_BANK(1, "ssokbank"),
    KAKAO_BANK(2, "kakaobank"),
    KOOKMIN_BANK(3, "kbbank"),
    SHINHAN_BANK(4, "shinhanbank"),
    WOORI_BANK(5, "wooribank"),
    HANA_BANK(6, "kebbank"),
    NH_BANK(7, "nhbank"),
    IBK_BANK(8, "ibkbank"),
    K_BANK(9, "kbank"),
    TOSS_BANK(10, "tossbank");

    private final int idx;
    private final String value;
}

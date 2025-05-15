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

    public static BankCode valueOf(int input) {
        switch(input) {
            case 1: return BankCode.SSOK_BANK;
            case 2: return BankCode.KAKAO_BANK;
            case 3: return BankCode.KOOKMIN_BANK;
            case 4: return BankCode.SHINHAN_BANK;
            case 5: return BankCode.WOORI_BANK;
            case 6: return BankCode.HANA_BANK;
            case 7: return BankCode.NH_BANK;
            case 8: return BankCode.IBK_BANK;
            case 9: return BankCode.K_BANK;
            case 10: return BankCode.TOSS_BANK;
            default: return null;
        }
    }
}

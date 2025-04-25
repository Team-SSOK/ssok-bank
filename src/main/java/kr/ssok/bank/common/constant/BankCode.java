package kr.ssok.bank.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BankCode {
    SSOK_BANK(1, "SSOK뱅크"),
    KAKAO_BANK(2, "카카오뱅크"),
    KOOKMIN_BANK(3, "KB국민은행"),
    SHINHAN_BANK(4, "신한은행"),
    WOORI_BANK(5, "우리은행"),
    HABA_BANK(6, "KEB하나은행"),
    NONGHYUP_BANK(7, "NH농협은행"),
    INDUSTRIAL_BANK(8, "IBK기업은행"),
    K_BANK(9, "케이뱅크"),
    TOSS_BANK(10, "토스뱅크");

    private final int idx;
    private final String value;
}

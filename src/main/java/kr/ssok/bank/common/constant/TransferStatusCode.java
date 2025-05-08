package kr.ssok.bank.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TransferStatusCode { // 송금 상태 코드
    // 정상
    SUCCESS(0,"SUCCESS"),
    // 실패
    FAILED(1,"FAILED"),
    // 보상 완료
    COMPENSATED(2, "COMPENSATED"),
    // 보상 실패
    COMPENSATION_FAILED(3,"COMPENSATION_FAILED");

    private final int idx;
    private final String value;
}

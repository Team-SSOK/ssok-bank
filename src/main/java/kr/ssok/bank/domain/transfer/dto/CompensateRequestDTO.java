package kr.ssok.bank.domain.transfer.dto;

import kr.ssok.bank.common.constant.BankCode;
import kr.ssok.bank.common.constant.CurrencyCode;
import lombok.Getter;

@Getter
public class CompensateRequestDTO {
    private String transactionId; // 오픈뱅킹 트랜잭션 ID
}

package kr.ssok.bank.domain.transfer.dto;

import kr.ssok.bank.common.constant.BankCode;
import kr.ssok.bank.common.constant.CurrencyCode;
import lombok.Getter;

@Getter
public class TransferDepositRequestDTO {
    private String transactionId; // 오픈뱅킹 트랜잭션 ID
    private BankCode depositBankCode; // 입금 은행 코드
    private String depositAccount; // 입금 계좌
    private Long transferAmount; // 입금 금액
    private CurrencyCode currencyCode; // 통화 코드
    private String counterAccount; // 출금 계좌
    private BankCode counterBankCode; // 출금 은행 코드
}

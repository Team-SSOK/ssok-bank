package kr.ssok.bank.domain.account.dto;

import kr.ssok.bank.common.constant.CurrencyCode;
import kr.ssok.bank.common.constant.TransferTypeCode;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountTransferHistoryResponseDTO {

    private TransferTypeCode transferType;
    private String account;
    private String counterpartAccount;
    private Long transferAmount;
    private CurrencyCode currencyCode;
    private LocalDateTime createdAt;

}

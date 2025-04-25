package kr.ssok.bank.domain.account.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AccountBalanceResponseDTO {
    private Long balance;
}

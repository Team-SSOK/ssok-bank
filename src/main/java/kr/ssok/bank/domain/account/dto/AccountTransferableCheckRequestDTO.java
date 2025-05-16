package kr.ssok.bank.domain.account.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AccountTransferableCheckRequestDTO {
    private String username;
    private String account;
    private Long transferAmount;
}

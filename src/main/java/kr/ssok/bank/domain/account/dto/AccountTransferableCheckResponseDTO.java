package kr.ssok.bank.domain.account.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
@AllArgsConstructor
public class AccountTransferableCheckResponseDTO {
    private Long balance;
    private Long withdrawLimit;
    @Setter
    private boolean isTransferable;
}

package kr.ssok.bank.domain.account.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class AccountResponseDTO {
    private String accountNumber;
    private long balance;
    private int bankCode;
    private int accountStatusCode;
    private int accountTypeCode;
    private long withdrawLimit;
    private LocalDate createdAt;
    private LocalDate updatedAt;
}

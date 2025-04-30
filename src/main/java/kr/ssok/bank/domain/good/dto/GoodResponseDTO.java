package kr.ssok.bank.domain.good.dto;

import kr.ssok.bank.domain.good.entity.Good;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class GoodResponseDTO {
    private Long id;
    private String name;
    private String accountTypeCode;
    private double interestRate;
    private int interestCycle;

    public static GoodResponseDTO from(Good good) {
        return GoodResponseDTO.builder()
                .id(good.getGoodId())
                .name(good.getName())
                .accountTypeCode(good.getAccountTypeCode().name())
                .interestRate(good.getInterestRate())
                .interestCycle(good.getInterestCycle())
                .build();
    }
}
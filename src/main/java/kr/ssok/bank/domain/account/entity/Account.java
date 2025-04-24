package kr.ssok.bank.domain.account.entity;

import jakarta.persistence.*;
import kr.ssok.bank.common.constant.BankCode;
import kr.ssok.bank.common.entity.TimeStamp;
import lombok.Getter;

@Entity
@Getter
public class Account extends TimeStamp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long accountId;

    @Column(name = "balance" , nullable = false)
    private Long balance;

    @Enumerated(EnumType.STRING)
    @Column(name = "bank_code", nullable = false)
    private BankCode bankCode;

    //TODO 상태 코드

    //TODO 출금한도

    //TODO 계좌 유형코드

}

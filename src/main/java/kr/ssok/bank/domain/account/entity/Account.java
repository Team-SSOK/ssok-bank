package kr.ssok.bank.domain.account.entity;

import jakarta.persistence.*;
import kr.ssok.bank.common.constant.BankCode;
import kr.ssok.bank.common.entity.TimeStamp;
import kr.ssok.bank.domain.user.entity.User;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Entity
@Getter
public class Account extends TimeStamp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long accountId;

    //계좌번호
    @Column(name = "account_number" , nullable = false, unique = true)
    private String accountNumber;

    //잔액
    @Column(name = "balance" , nullable = false)
    private Long balance;

    //은행 코드
    @Enumerated(EnumType.STRING)
    @Column(name = "bank_code", nullable = false)
    private BankCode bankCode;

    //계좌 상태 코드
    @Enumerated(EnumType.STRING)
    @Column(name = "account_status_code", nullable = false)
    private BankCode accountStatusCode;

    //출금 한도
    @Column(name = "withdraw_limit" , nullable = false)
    private Long withdrawLimit;

    //계좌 유형 코드
    @Enumerated(EnumType.STRING)
    @Column(name = "account_type_code", nullable = false)
    private BankCode accountTypeCode;

    private LocalDateTime convertToLocalDateTime(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
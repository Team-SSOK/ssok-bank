package kr.ssok.bank.domain.account.entity;

import jakarta.persistence.*;
import kr.ssok.bank.common.constant.AccountStatusCode;
import kr.ssok.bank.common.constant.AccountTypeCode;
import kr.ssok.bank.common.constant.BankCode;
import kr.ssok.bank.common.entity.TimeStamp;
import kr.ssok.bank.domain.transfer.entity.TransferHistory;
import kr.ssok.bank.domain.user.entity.User;
import lombok.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account extends TimeStamp {

    @Id
    @Column(name = "account_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long accountId;

    //계좌 유형 코드
    @Enumerated(EnumType.STRING)
    @Column(name = "account_type_code", nullable = false)
    private AccountTypeCode accountTypeCode;

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
    private AccountStatusCode accountStatusCode;

    public boolean isDormant() {
        return this.accountStatusCode == AccountStatusCode.DORMANT;
    }

    //출금 한도
    @Column(name = "withdraw_limit" , nullable = false)
    private Long withdrawLimit;

    private LocalDateTime convertToLocalDateTime(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    // User
    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // TransferHistory
    @Setter
    @OneToMany(mappedBy = "account", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<TransferHistory> transferHistories = new ArrayList<>();

}
package kr.ssok.bank.domain.transfer.entity;

import jakarta.persistence.*;
import kr.ssok.bank.common.constant.CurrencyCode;
import kr.ssok.bank.common.constant.TransferTypeCode;
import kr.ssok.bank.common.entity.TimeStamp;
import kr.ssok.bank.domain.account.entity.Account;
import kr.ssok.bank.domain.user.entity.User;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Getter
@Entity
public class TransferHistory extends TimeStamp {

    @Id
    @Column(name = "transfer_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transferId;

    // 오픈뱅킹 트랜잭션 ID
    @Column(name = "transaction_id", nullable = false, unique = true)
    private String transactionId;

    // 송금 타입
    @Column(name = "transfer_type", nullable = false)
    private TransferTypeCode transferTypeCode;

    // 상대 계좌 ID
    @Column(name = "counterpart_account", nullable = false)
    private Long counterpartAccount;

    // 송금 금액
    @Column(name = "transfer_amount", nullable = false)
    private Long transferAmount = 0L;

    // 통화 코드
    @Enumerated(EnumType.STRING)
    @Column(name = "currency_code", nullable = false)
    private CurrencyCode currencyCode;

    // 송금 후 잔액
    @Column(name = "balance_after", nullable = false)
    private Long balanceAfter = 0L;

    private LocalDateTime convertToLocalDateTime(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    // Account
    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

}

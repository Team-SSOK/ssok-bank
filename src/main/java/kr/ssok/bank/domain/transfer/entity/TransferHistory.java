package kr.ssok.bank.domain.transfer.entity;

import jakarta.persistence.*;
import kr.ssok.bank.common.constant.CurrencyCode;
import kr.ssok.bank.common.constant.TransferTypeCode;
import kr.ssok.bank.common.entity.TimeStamp;
import kr.ssok.bank.domain.account.entity.Account;
import lombok.*;

import static kr.ssok.bank.common.constant.CurrencyCode.WON;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"transaction_id", "transfer_type"})
})
public class TransferHistory extends TimeStamp {

    @Id
    @Column(name = "transfer_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transferId;

    // 오픈뱅킹 트랜잭션 ID
    @Column(name = "transaction_id", nullable = false)
    private String transactionId;

    // 송금 타입
    @Column(name = "transfer_type", nullable = false)
    private TransferTypeCode transferTypeCode;

    // 상대 계좌 ID
    @Column(name = "counterpart_account", nullable = false)
    private String counterpartAccount;

    // 송금 금액
    @Column(name = "transfer_amount", nullable = false)
    private Long transferAmount = 0L;

    // 통화 코드
    @Enumerated(EnumType.STRING)
    @Column(name = "currency_code", nullable = false)
    private CurrencyCode currencyCode = WON;

    // 송금 후 잔액
    @Column(name = "balance_after", nullable = false)
    private Long balanceAfter = 0L;

    // Account
    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;
}

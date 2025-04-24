package kr.ssok.bank.domain.transfer.entity;

import jakarta.persistence.*;
import kr.ssok.bank.common.constant.CurrencyCode;
import kr.ssok.bank.common.entity.TimeStamp;
import lombok.Getter;

@Getter
@Entity
public class Transfer extends TimeStamp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transferId;

    @Column(name = "counterpart_account", nullable = false)
    private Long counterpartAccount;

    @Column(name = "transfer_amount", nullable = false)
    private Long transferAmount = 0L;

    @Enumerated(EnumType.STRING)
    @Column(name = "currency_code", nullable = false)
    private CurrencyCode currencyCode;

    @Column(name = "balance", nullable = false)
    private Long balance = 0L;

}

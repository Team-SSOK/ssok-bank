package kr.ssok.bank.domain.good.entity;

import jakarta.persistence.*;
import kr.ssok.bank.common.constant.AccountTypeCode;
import kr.ssok.bank.common.entity.TimeStamp;
import kr.ssok.bank.domain.account.entity.Account;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "good")
public class Good extends TimeStamp {

    @Id
    @Column(name = "good_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long goodId;

    @Column(name = "name")
    private String name; // 상품명

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type_code", nullable = false)
    private AccountTypeCode accountTypeCode; // 계좌 유형 코드 (예금/적금 등)

    @Column(name = "interest_rate", nullable = false)
    private Double interestRate; // 연이자율 (예: 1.5%)

    @Column(name = "interest_cycle", nullable = false)
    private Integer interestCycle; // 이자 지급 주기 (일 단위, 예: 30 = 매월)

    @OneToMany(mappedBy = "good", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Account> accounts = new ArrayList<>();
}

package kr.ssok.bank.domain.user.entity;

import jakarta.persistence.*;
import kr.ssok.bank.common.constant.UserTypeCode;
import kr.ssok.bank.common.entity.TimeStamp;
import kr.ssok.bank.domain.account.entity.Account;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends TimeStamp {

    @Id
    @Column(name = "user_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "phone_number", length = 64 , nullable = false)
    private String phoneNumber;

    @Column(name = "daily_transaction_total", nullable = false)
    private Long dailyTransactionTotal = 0L;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_type_code", nullable = false)
    private UserTypeCode userTypeCode;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Account> accounts = new ArrayList<>();

    public void addAccount(Account account) {
        this.accounts.add(account);
        account.setUser(this);
    }

    private LocalDateTime convertToLocalDateTime(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

}

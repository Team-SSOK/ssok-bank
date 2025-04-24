package kr.ssok.bank.domain.user.entity;

import jakarta.persistence.*;
import kr.ssok.bank.common.entity.TimeStamp;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Entity
public class User extends TimeStamp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(name = "username", unique = true , nullable = false)
    private String username;

    @Column(name = "phone_number", length = 64 , nullable = false)
    private String phoneNumber;

    @Column(name = "daily_transaction_total", nullable = false)
    private Long dailyTransactionTotal = 0L;

//    @Column(length = 250)
//    private String accessToken;
//    private LocalDateTime accessTokenExpirationTime;

//    @Enumerated(EnumType.STRING)
//    @Column(name = "user_role", nullable = false)
//    private UserRole userRole;

    private LocalDateTime convertToLocalDateTime(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

}

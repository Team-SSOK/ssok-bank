package kr.ssok.bank.scheduler;

import kr.ssok.bank.common.constant.AccountStatusCode;
import kr.ssok.bank.common.constant.CurrencyCode;
import kr.ssok.bank.common.constant.TransferTypeCode;
import kr.ssok.bank.domain.account.entity.Account;
import kr.ssok.bank.domain.account.repository.AccountRepository;
import kr.ssok.bank.domain.good.entity.Good;
import kr.ssok.bank.domain.transfer.entity.TransferHistory;
import kr.ssok.bank.domain.transfer.repository.TransferRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class InterestScheduler {

    private final AccountRepository accountRepository;
    private final TransferRepository transferHistoryRepository;

    // 매일 오전 1시에 실행 (cron 형식: 초 분 시 일 월 요일)
    @Scheduled(cron = "0 0 1 * * *")
    @Transactional
    public void applyInterestToAccounts() {
        log.info(">>> 이자 지급 스케줄러 실행 시작");

        List<Account> accounts = accountRepository.findByAccountStatusCodeAndBalanceGreaterThan(AccountStatusCode.ACTIVE, 0L);

        for (Account account : accounts) {
            try {
                Good good = account.getGood();

                if (good == null || good.getInterestRate() == null || good.getInterestCycle() == null) {
                    log.warn(">>> {} 계좌의 상품 정보가 잘못되었거나 설정되지 않았습니다.", account.getAccountNumber());
                    continue;
                }

                // 계좌 개설일 기준으로 이자 주기 지났는지 확인
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime lastInterestPaidAt = account.getLastInterestPaidAt() != null
                        ? account.getLastInterestPaidAt()
                        : account.getCreatedAt();

                long daysSinceLastInterest = java.time.Duration.between(lastInterestPaidAt, now).toDays();
                if (daysSinceLastInterest < good.getInterestCycle()) {
                    log.info(">>> {} 계좌는 이자 지급 주기가 지나지 않았습니다.", account.getAccountNumber());
                    continue;
                }

                // 이자 계산
                BigDecimal interestAmount = new BigDecimal(account.getBalance())
                        .multiply(BigDecimal.valueOf(good.getInterestRate()))
                        .divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP);

                // 잔액 갱신
                BigDecimal currentBalance = new BigDecimal(account.getBalance());
                BigDecimal newBalance = currentBalance.add(interestAmount);
                account.setBalance(newBalance.longValue());

                // 마지막 이자 지급 일시 갱신
                account.setLastInterestPaidAt(now);

                // 이자 지급 이체 내역 저장
                TransferHistory history = TransferHistory.builder()
                        .account(account)
                        .transactionId("interest-" + now.toString())
                        .transferTypeCode(TransferTypeCode.INTEREST) // 송금 타입
                        .counterpartAccount(account.getAccountNumber()) // 이체 대상 계좌 번호
                        .transferAmount(interestAmount.longValue()) // 이자 금액
                        .currencyCode(CurrencyCode.WON) // 통화 코드
                        .balanceAfter(account.getBalance()) // 이체 후 잔액
                        .build();

                transferHistoryRepository.save(history);

                log.info(">>> {} 계좌에 이자 지급 완료: + {}", account.getAccountNumber(), interestAmount);
            } catch (Exception e) {
                log.error(">>> {} 계좌 이자 지급 중 오류 발생: {}", account.getAccountNumber(), e.getMessage(), e);
            }
        }

        log.info(">>> 이자 지급 스케줄러 실행 종료");
    }
}

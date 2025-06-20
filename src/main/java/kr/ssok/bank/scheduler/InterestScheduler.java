package kr.ssok.bank.scheduler;

import kr.ssok.bank.common.constant.*;
import kr.ssok.bank.common.util.AESUtil;
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
    private final AESUtil aesUtil;

    // 매일 오전 1시에 실행 (cron 형식: 초 분 시 일 월 요일)
    @Scheduled(cron = "0 0 1 * * *")
    @Transactional
    public void applyInterestToAccounts() {
        log.info("[이자 지급] 스케줄러 실행 시작...");

        List<Account> accounts = accountRepository
                .findByAccountStatusCodeAndBalanceGreaterThanAndBankCode(AccountStatusCode.ACTIVE, 0L, BankCode.SSOK_BANK);

        for (Account account : accounts) {
            try {
                Good good = account.getGood();

                if (good == null || good.getInterestRate() == null || good.getInterestCycle() == null) {
                    log.warn("[이자 지급] {} 계좌의 상품 정보가 잘못되었거나 설정되지 않았습니다.", account.getAccountNumber());
                    continue;
                }

                // 계좌 개설일 기준으로 이자 주기 지났는지 확인
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime lastInterestPaidAt = account.getLastInterestPaidAt() != null
                        ? account.getLastInterestPaidAt()
                        : account.getCreatedAt();

                long daysSinceLastInterest = java.time.Duration.between(lastInterestPaidAt, now).toDays();
                if (daysSinceLastInterest < good.getInterestCycle()) {
                    log.info("[이자 지급] {} 계좌는 이자 지급 주기가 지나지 않았습니다.", account.getAccountNumber());
                    continue;
                }

                // 이자 계산 (연이율 → 일이율 적용)
                BigDecimal annualRate = BigDecimal.valueOf(good.getInterestRate());
                BigDecimal dailyRate = annualRate.divide(BigDecimal.valueOf(100 * 365), 10, BigDecimal.ROUND_HALF_UP);

                /* 연이율 (기존 코드)
                BigDecimal interestAmount = new BigDecimal(account.getBalance())
                        .multiply(annualRate)
                        .divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP);
                        */

                // 일이율
                BigDecimal interestAmount = new BigDecimal(account.getBalance())
                        .multiply(dailyRate)
                        .setScale(0, BigDecimal.ROUND_DOWN); // 정수 처리

                if (interestAmount.compareTo(BigDecimal.ZERO) == 0) {
                    log.info("[이자 지급] {} 계좌는 이자 금액이 0으로 지급되지 않습니다.", account.getAccountNumber());
                    continue;
                }

                // 잔액 갱신
                BigDecimal currentBalance = new BigDecimal(account.getBalance());
                BigDecimal newBalance = currentBalance.add(interestAmount);
                account.setBalance(newBalance.longValue());

                // 마지막 이자 지급 일시 갱신
                account.setLastInterestPaidAt(now);

                // 이자 지급 이체 내역 저장
                String encryptedAccountNumber = aesUtil.encrypt(account.getAccountNumber());

                TransferHistory history = TransferHistory.builder()
                        .account(account)
                        .transactionId("interest-" + now.toString())
                        .transferTypeCode(TransferTypeCode.INTEREST) // 송금 타입
                        .transferStatusCode(TransferStatusCode.SUCCESS) // 송금 상태 코드
                        .counterpartAccount(encryptedAccountNumber) // 이체 대상 계좌 번호
                        .transferAmount(interestAmount.longValue()) // 이자 금액
                        .currencyCode(CurrencyCode.WON) // 통화 코드
                        .balanceAfter(account.getBalance()) // 이체 후 잔액
                        .build();

                transferHistoryRepository.save(history);

                String maskedAccountNumber = account.getAccountNumber().replaceAll("(\\d{2})\\d+(\\d{2})", "$1****$2");
                log.info("[이자 지급] 지급 완료 : 계좌 = {}, 금액 = {}", maskedAccountNumber, interestAmount);

            } catch (Exception e) {
                log.error("[이자 지급] 처리 중 오류 발생 : {}, {}", account.getAccountNumber(), e.getMessage(), e);
            }
        }

        log.info("[이자 지급] 스케줄러 실행 종료");
    }
}

package kr.ssok.bank.domain.account.service;

import kr.ssok.bank.common.constant.AccountStatusCode;
import kr.ssok.bank.common.constant.AccountTypeCode;
import kr.ssok.bank.common.constant.BankCode;
import kr.ssok.bank.common.exception.BaseException;
import kr.ssok.bank.domain.account.entity.Account;
import kr.ssok.bank.domain.account.repository.AccountRepository;
import kr.ssok.bank.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountServiceImpl implements AccountService{

    private final AccountRepository accountRepository;

    // 계좌 생성 메서드
    public Account createAccount(User user, AccountTypeCode accountTypeCode) throws BaseException {
        try {
            log.info("Creating account for user: {}", user.getUsername());

            // 계좌 생성 시 빌더 패턴을 사용
            Account account = Account.builder()
                    .accountTypeCode(accountTypeCode)
                    .accountNumber(generateAccountNumber(accountTypeCode))
                    .balance(0L) // 초기 잔액 0
                    .bankCode(BankCode.SSOK_BANK) // 은행 코드 처리
                    .accountStatusCode(AccountStatusCode.ACTIVE) // 기본 활성
                    .withdrawLimit(300000L) // 출금 한도 30만원
                    .user(user)
                    .build();

            log.info("Account successfully created for user: {}", user.getUsername());

            // 계좌 저장
            return accountRepository.save(account);
        } catch (Exception e) {
            log.error("Failed to create account for user: {}", user.getUsername(), e);
            throw new RuntimeException("Account creation failed", e);
        }

    }

    // 계좌번호 채번 메서드 (중복 체크 포함)
    private String generateAccountNumber(AccountTypeCode accountTypeCode) {
        String bankPrefix = "626"; //SSOK 뱅크 계좌 Prefix 임의 지정 (LG CNS AM 종강일자)

        //계좌 유형 고려
        String typeCode = String.format("%02d", accountTypeCode.getIdx()); // 예: 01, 02, 03

        //고유 번호 생성
        String accountNumber;
        do {
            int randomPart = (int)(Math.random() * 9000) + 1000; // 4자리 랜덤
            long timePart = System.currentTimeMillis() % 1_000_000L; // 6자리 시간 기반

            accountNumber = String.format("%s-%s-%04d-%06d", bankPrefix, typeCode, randomPart, timePart);
        } while (accountRepository.existsByAccountNumber(accountNumber));

        return accountNumber;
    }

}

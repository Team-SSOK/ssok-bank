package kr.ssok.bank.domain.account.service;

import jakarta.persistence.EntityNotFoundException;
import kr.ssok.bank.common.constant.AccountStatusCode;
import kr.ssok.bank.common.constant.AccountTypeCode;
import kr.ssok.bank.common.constant.BankCode;
import kr.ssok.bank.domain.account.entity.Account;
import kr.ssok.bank.domain.account.repository.AccountRepository;
import kr.ssok.bank.domain.user.entity.User;
import kr.ssok.bank.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountServiceImpl {

    private final AccountRepository accountRepository;

    // 계좌 생성 메서드
    public Account createAccount(User user, BankCode bankCode, AccountTypeCode accountTypeCode) {
        Account account = new Account();
//        Account account = Account.builder();

        // 1. 계좌유형 코드 설정
        account.setAccountTypeCode(accountTypeCode);

        // 2. 계좌번호 채번 (예시: 110-1234-567890)
        account.setAccountNumber(generateAccountNumber(bankCode, accountTypeCode));
        //accountNumber(generateAccountNumber(bankCode));

        // 3. 초기 잔액 설정 (0원)
        account.setBalance(0L);

        // 4. 은행 코드 설정 (1: 쏙, 2: 카뱅)
        account.setBankCode(BankCode.SSOK_BANK);

        // 5. 계좌 상태 코드 설정 (0: 휴면, 1: 활성)
        account.setAccountStatusCode(AccountStatusCode.ACTIVE); // 기본 활성

        // 6. 출금 한도 설정 (일반 개인&예금: 하루 30만원)
        account.setWithdrawLimit(300000L); // 30만원

        // 7. 사용자 설정
        account.setUser(user);

        // 계좌 저장
        return accountRepository.save(account);
    }

    // 계좌번호 채번 메서드 (중복 체크 포함)
    private String generateAccountNumber(BankCode bankCode, AccountTypeCode accountTypeCode) {
        String bankPrefix;

        //은행 코드 고려
        switch (bankCode) {
            case SSOK_BANK -> bankPrefix = "626";
            default -> bankPrefix = "999";
        }

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

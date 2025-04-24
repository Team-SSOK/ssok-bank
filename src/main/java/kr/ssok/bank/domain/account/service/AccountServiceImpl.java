package kr.ssok.bank.domain.account.service;

import jakarta.persistence.EntityNotFoundException;
import kr.ssok.bank.domain.account.entity.Account;
import kr.ssok.bank.domain.account.repository.AccountRepository;
import kr.ssok.bank.domain.user.entity.User;
import kr.ssok.bank.domain.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AccountServiceImpl {
/*
    private final AccountRepository accountRepository;

    // 계좌 생성 메서드
    public Account createAccount(User user, BankCode accountTypeCode, BankCode bankCode) {
        Account account = new Account();

        // 1. 계좌유형 코드 설정
        account.setAccountTypeCode(accountTypeCode);

        // 2. 계좌번호 채번 (예시: 110-1234-567890)
        account.setAccountNumber(generateAccountNumber(bankCode));

        // 3. 초기 잔액 설정 (0원)
        account.setBalance(0L);

        // 4. 은행 코드 설정 (1: 쏙, 2: 카뱅)
        account.setBankCode(bankCode);

        // 5. 계좌 상태 코드 설정 (0: 휴면, 1: 활성)
        account.setAccountStatusCode(BankCode.HYUMYEON); // 기본 휴면

        // 6. 출금 한도 설정 (일반 개인&예금: 하루 30만원)
        account.setWithdrawLimit(300000L); // 30만원

        // 7. 사용자 설정
        account.setUser(user);

        // 계좌 저장
        return accountRepository.save(account);
    }

    // 계좌번호 채번 메서드
    private String generateAccountNumber(BankCode bankCode) {
        String bankCodeStr = bankCode.getCode(); // 은행 코드 (예: 110)
        int random = (int)(Math.random() * 9000) + 1000; // 랜덤 4자리 (1000~9999)
        return bankCodeStr + "-" + random + "-" + System.currentTimeMillis(); // 예시: 110-1234-567890
    }*/
}

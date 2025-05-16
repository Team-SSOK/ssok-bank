package kr.ssok.bank.domain.account.service;

import kr.ssok.bank.common.constant.AccountStatusCode;
import kr.ssok.bank.common.constant.AccountTypeCode;
import kr.ssok.bank.common.constant.BankCode;
import kr.ssok.bank.common.exception.BaseException;
import kr.ssok.bank.common.constant.FailureStatusCode;
import kr.ssok.bank.common.util.AESUtil;
import kr.ssok.bank.domain.account.dto.AccountResponseDTO;
import kr.ssok.bank.domain.account.entity.Account;
import kr.ssok.bank.domain.account.repository.AccountRepository;
import kr.ssok.bank.domain.good.entity.Good;
import kr.ssok.bank.domain.user.entity.User;
import kr.ssok.bank.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService{

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final AESUtil aesUtil;

    // 계좌 생성 메서드
    public Account createAccount(User user, AccountTypeCode accountTypeCode, Good good) throws BaseException {
        try {
            log.info("사용자 생성 시작: {}", user.getUsername());

            // 계좌 생성 시 빌더 패턴을 사용
            Account account = Account.builder()
                    .accountTypeCode(accountTypeCode)
                    .accountNumber(aesUtil.encrypt(generateAccountNumber(accountTypeCode)))
                    .balance(4967500L) // 초기 잔액 임의 셋팅
                    .bankCode(BankCode.SSOK_BANK) // 은행 코드 처리
                    .accountStatusCode(AccountStatusCode.ACTIVE) // 기본 활성
                    .withdrawLimit(300000L) // 출금 한도 30만원
                    .user(user)
                    .good(good)
                    .build();

            log.info("사용자 생성 완료: {}", user.getUsername());

            // 계좌 저장
            return accountRepository.save(account);
        } catch (Exception e) {
            log.error("계좌 생성 중 오류 발생: {}", user.getUsername(), e);
            throw new BaseException(FailureStatusCode.ACCOUNT_CREATE_FAILED);
        }

    }

    @Override
    public Account getAccountByAccountNumber(String accountNumber) throws BaseException {
        String encryptedAccountNumber = aesUtil.encrypt(accountNumber);

        return this.accountRepository.findAccountByAccountNumber(encryptedAccountNumber).orElseThrow(()->
                new BaseException(FailureStatusCode.ACCOUNT_NOT_FOUND)
        );
    }

    // 사용자 별 계좌 조회 메서드
    @Override
    public List<AccountResponseDTO> getAccountsByUsernameAndPhoneNumber(String username, String phoneNumber) {

        log.info("계좌 조회 요청: username = {} , phoneNumber = {}", username, phoneNumber);

        // 사용자 조회
        User user = userRepository.findByUsernameAndPhoneNumber(username, phoneNumber)
                .orElseThrow(() -> {
                    log.warn("사용자 정보 없음: username = {}, phoneNumber = {}", username, phoneNumber);
                    return new BaseException(FailureStatusCode.USER_NOT_FOUND);
                });

        // 사용자의 전체 계좌 조회
        List<Account> accounts = accountRepository.findAllByUser(user);

        List<AccountResponseDTO> result = accounts.stream().map(account -> {
                    // createdAt 필드가 null일 경우 현재 날짜로 설정
                    LocalDate createdAt = (account.getCreatedAt() != null) ? account.getCreatedAt().toLocalDate() : LocalDate.now();

                    return AccountResponseDTO.builder()
                            .accountNumber(aesUtil.decrypt(account.getAccountNumber()))
                            .balance(account.getBalance())
                            .bankCode(account.getBankCode().getIdx())
                            .accountStatusCode(account.getAccountStatusCode().getIdx())
                            .accountTypeCode(account.getAccountTypeCode().getIdx())
                            .withdrawLimit(account.getWithdrawLimit())
                            .createdAt(createdAt)  // null일 경우 기본값을 현재 날짜로 설정
                            .updatedAt(account.getUpdatedAt() != null ? account.getUpdatedAt().toLocalDate() : LocalDate.now())
                            .build();
                })
                .collect(Collectors.toList());

        log.info("계좌 조회 성공: username = {}, 계좌 수 = {}", username, result.size());

        return result;
    }

    // 휴면 계좌 여부 확인 메서드
    @Override
    public boolean isAccountDormant(String accountNumber) {
        String encryptedAccountNumber = aesUtil.encrypt(accountNumber);

        Account account = accountRepository.findAccountByAccountNumber(encryptedAccountNumber)
                .orElseThrow(() -> new BaseException(FailureStatusCode.ACCOUNT_NOT_FOUND));

        return account.isDormant();
    }

    // 계좌번호 채번 메서드
    private String generateAccountNumber(AccountTypeCode accountTypeCode) {
        //SSOK 뱅크 계좌 Prefix 임의 지정 (LG CNS AM 종강일자)
        String bankPrefix = "626";

        //계좌 유형 고려 (예: 01 예금, 02 적금, 03 청약)
        String typeCode = String.format("%02d", accountTypeCode.getIdx());

        String formattedAccountNumber;

        do {
            int randomPart = (int)(Math.random() * 9000) + 1000; // 4자리 랜덤
            long timeBase = System.currentTimeMillis() % 100_000L; // 5자리 시간
            String checkDigitSource = String.format("%02d%04d%05d", accountTypeCode.getIdx(), randomPart, timeBase);

            // 검증 번호
            int checkDigit = calculateLuhnCheckDigit(checkDigitSource);

            // 계좌번호 마지막 자리를 검증 번호로 대체
            long timePartWithCheck = timeBase * 10 + checkDigit; // 예: 12345 → 123451

            formattedAccountNumber = String.format("%s-%s-%04d-%06d", bankPrefix, typeCode, randomPart, timePartWithCheck);
        } while (accountRepository.existsByAccountNumber(formattedAccountNumber));

        return formattedAccountNumber;
    }

    // 검증번호 체크 알고리즘
    private int calculateLuhnCheckDigit(String number) {
        int sum = 0;
        boolean alternate = false;
        for (int i = number.length() - 1; i >= 0; i--) {
            int n = Character.getNumericValue(number.charAt(i));
            if (alternate) {
                n *= 2;
                if (n > 9) n -= 9;
            }
            sum += n;
            alternate = !alternate;
        }
        return (10 - (sum % 10)) % 10;
    }
}
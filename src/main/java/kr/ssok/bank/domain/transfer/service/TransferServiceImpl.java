package kr.ssok.bank.domain.transfer.service;

import kr.ssok.bank.common.constant.FailureStatusCode;
import kr.ssok.bank.common.constant.TransferStatusCode;
import kr.ssok.bank.common.constant.TransferTypeCode;
import kr.ssok.bank.common.exception.BaseException;
import kr.ssok.bank.common.util.AESUtil;
import kr.ssok.bank.domain.account.entity.Account;
import kr.ssok.bank.domain.account.repository.AccountRepository;
import kr.ssok.bank.domain.transfer.dto.CompensateRequestDTO;
import kr.ssok.bank.domain.transfer.dto.TransferDepositRequestDTO;
import kr.ssok.bank.domain.transfer.dto.TransferWithdrawRequestDTO;
import kr.ssok.bank.domain.transfer.entity.TransferHistory;
import kr.ssok.bank.domain.transfer.repository.TransferRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferServiceImpl implements TransferService{

    private Map<String, ReentrantLock> lockMap = new HashMap<>();

    private final AccountRepository accountRepository;
    private final TransferRepository transferRepository;
    private final AESUtil aesUtil;

    // 출금 이체
    @Transactional
    public void withdraw(TransferWithdrawRequestDTO dto) throws BaseException {
        log.info("[출금 이체] 서비스 진입: 출금 계좌 = {}", dto.getWithdrawAccount());

        // 0. 중복 transactionId 방지
        if (transferRepository.existsByTransactionIdAndTransferTypeCode(dto.getTransactionId(), TransferTypeCode.WITHDRAW)) {
            log.error("[출금 이체] 서비스 처리 실패: 중복 transferId = {}", dto.getTransactionId());
            throw new BaseException(FailureStatusCode.DUPLICATED_TRANSACTION_ID);
        }

        // 유효하지 않은 금액 방지
        if (dto.getTransferAmount() <= 0) {
            log.error("[출금 이체] 서비스 처리 실패: 유효하지 않은 금액 = {}", dto.getTransferAmount());
            throw new BaseException(FailureStatusCode.INVALID_TRANSFER_AMOUNT);
        }

        // 1. 출금 계좌 락 걸고 조회
        // 1-1. 암호화: 출금 계좌번호
        log.info("[출금 이체] 출금 계좌 암호화 처리 전: 계좌번호 = {}", dto.getWithdrawAccount());
        String encrypted = aesUtil.encrypt(dto.getWithdrawAccount());
        log.info("[출금 이체] 출금 계좌 암호화 처리 완료: 계좌번호 = {} ", encrypted);

        Account withdrawAccount = accountRepository.findWithPessimisticLockByAccountNumber(encrypted)
                .orElseThrow(() -> new BaseException(FailureStatusCode.ACCOUNT_NOT_FOUND));

        // 2. 출금 가능 여부 확인 및 처리
        if (withdrawAccount.getBalance() < dto.getTransferAmount()) {
            log.error("[출금 이체] 출금 계좌 잔액 부족 : 출금 계좌 잔액 = {} < 거래 요청 금액 = {}"
                                    , withdrawAccount.getBalance(), dto.getTransferAmount());
            throw new BaseException(FailureStatusCode.TRANSFER_NO_BALANCE);
        }
        withdrawAccount.withdraw(dto.getTransferAmount());

        // 3. 출금 내역 기록
        // 3-1. 암호화: 상대 계좌 (입금 계좌)
        log.info("[출금 이체] 입금 계좌 암호화 처리 전: 계좌번호 = {}", dto.getCounterAccount());
        String encryptedCounterAccount = aesUtil.encrypt(dto.getCounterAccount());
        log.info("[출금 이체] 입금 계좌 암호화 처리 완료: 계좌번호 = {} ", encryptedCounterAccount);

        TransferHistory history = TransferHistory.builder()
                .account(withdrawAccount)
                .counterpartAccount(encryptedCounterAccount) // 암호화해서 저장
                .transferAmount(dto.getTransferAmount())  // 송금 금액 기록
                .balanceAfter(withdrawAccount.getBalance())  // 출금 후 잔액 기록
                .transferTypeCode(TransferTypeCode.WITHDRAW)
                .transferStatusCode(TransferStatusCode.SUCCESS)
                .currencyCode(dto.getCurrencyCode())
                .transactionId(dto.getTransactionId())
                .build();
        transferRepository.save(history);

        // 4. 변경된 계좌 저장
        accountRepository.save(withdrawAccount);

        log.info("[출금 이체] 서비스 처리 완료");
    }

    // 입금 이체
    @Transactional
    public void deposit(TransferDepositRequestDTO dto) throws BaseException {
        // 0. 중복 transactionId 방지
        if (transferRepository.existsByTransactionIdAndTransferTypeCode(dto.getTransactionId(), TransferTypeCode.DEPOSIT)) {
            log.error("[입금 이체] 서비스 처리 실패: 중복 transferId = {}", dto.getTransactionId());
            throw new BaseException(FailureStatusCode.DUPLICATED_TRANSACTION_ID);
        }

        // 유효하지 않은 금액 방지
        if (dto.getTransferAmount() <= 0) {
            log.error("[입금 이체] 서비스 처리 실패: 유효하지 않은 금액 = {}", dto.getTransferAmount());
            throw new BaseException(FailureStatusCode.INVALID_TRANSFER_AMOUNT);
        }

        // 1. 선출금 내역 존재 확인
        boolean withdrawExists = transferRepository.existsByTransactionIdAndTransferTypeCode(dto.getTransactionId(), TransferTypeCode.WITHDRAW);
        if (!withdrawExists) {
            log.error("[입금 이체] 서비스 처리 실패: 선출금 기록 없음 (transactionId = {})", dto.getTransactionId());
            throw new BaseException(FailureStatusCode.MISSING_WITHDRAW_FOR_DEPOSIT);
        }

        // 2. 입금 계좌 락 걸고 조회
        // 2-1. 암호화: 입금 계좌번호
        log.info("[입금 이체] 입금 계좌 암호화 처리 전: 계좌번호 = {}", dto.getDepositAccount());
        String encrypted = aesUtil.encrypt(dto.getDepositAccount());
        log.info("[입금 이체] 입금 계좌 암호화 처리 완료: 계좌번호 = {} ", encrypted);

        Account depositAccount = accountRepository.findWithPessimisticLockByAccountNumber(encrypted)
                .orElseThrow(() -> new BaseException(FailureStatusCode.ACCOUNT_NOT_FOUND));

        // 3. 입금 처리
        depositAccount.deposit(dto.getTransferAmount());

        // 4. 입금 내역 기록
        // 4-1. 암호화: 상대 계좌 (출금 계좌)
        log.info("[입금 이체] 출금 계좌 암호화 처리 전: 계좌번호 = {}", dto.getCounterAccount());
        String encryptedCounterAccount = aesUtil.encrypt(dto.getCounterAccount());
        log.info("[입금 이체] 출금 계좌 암호화 처리 전: 계좌번호 = {}", encryptedCounterAccount);

        TransferHistory history = TransferHistory.builder()
                .account(depositAccount)
                .counterpartAccount(encryptedCounterAccount)  // 암호화해서 저장
                .transferAmount(dto.getTransferAmount())  // 송금 금액 기록
                .balanceAfter(depositAccount.getBalance())  // 입금 후 잔액 기록
                .transferTypeCode(TransferTypeCode.DEPOSIT)
                .transferStatusCode(TransferStatusCode.SUCCESS)
                .currencyCode(dto.getCurrencyCode())
                .transactionId(dto.getTransactionId())
                .build();

        transferRepository.save(history);

        // 5. 변경된 계좌 저장
        accountRepository.save(depositAccount);

        log.info("[입금 이체] 서비스 처리 완료");
    }

    // 보상 처리
    @Transactional
    public void compensate(CompensateRequestDTO compensateRequestDTO) throws BaseException {
        log.info("[보상] 서비스 진입 완료");

        // 1. 실패한 출금 내역을 찾는다.
        TransferHistory failedWithdrawal = transferRepository
                .findByTransactionIdAndTransferTypeCode(compensateRequestDTO.getTransactionId(), TransferTypeCode.WITHDRAW)
                .orElseThrow(() -> {
                    log.error("[보상] 실패: 실패한 출금 거래 내역 조회 불가. (transactionId = {})", compensateRequestDTO.getTransactionId());
                    return new BaseException(FailureStatusCode.TRANSACTION_NOT_EXISTS);
                });

        // 2. 이미 보상 처리된 경우, 예외 처리
        if (failedWithdrawal.getTransferStatusCode() == TransferStatusCode.COMPENSATED) {
            log.error("[보상] 실패: 사전에 완료된 보상 처리. (transferId = {})", compensateRequestDTO.getTransactionId());
            throw new BaseException(FailureStatusCode.TRANSFER_ALREADY_COMPENSATED);
        }

        // 유효하지 않은 금액 방지
        if (failedWithdrawal.getTransferAmount() <= 0) {
            log.error("[보상] 실패: 유효하지 않은 금액. (transferId = {}, 거래 요청 금액 = {})"
                    , compensateRequestDTO.getTransactionId(), failedWithdrawal.getTransferAmount());
            throw new BaseException(FailureStatusCode.INVALID_TRANSFER_AMOUNT);
        }

        // 3. 출금 계좌 찾기
        Account account = failedWithdrawal.getAccount();
        log.info("[보상] 보상 대상 계좌 조회 완료: 계좌번호 = {}", account.getAccountNumber());

        // 4. 보상 처리 (입금)
        account.deposit(failedWithdrawal.getTransferAmount()); // 실패한 금액을 다시 입금

        // 5. 보상 처리 후 TransferHistory에 보상 내역 추가
        transferRepository.save(TransferHistory.builder()
                .transactionId(compensateRequestDTO.getTransactionId())
                .transferTypeCode(TransferTypeCode.COMPENSATE)
                .counterpartAccount("SYSTEM")
                .transferAmount(failedWithdrawal.getTransferAmount())
                .currencyCode(failedWithdrawal.getCurrencyCode())
                .balanceAfter(account.getBalance())
                .account(account)
                .transferStatusCode(TransferStatusCode.COMPENSATED) // 보상 처리 완료 상태
                .build());

        // 6. 계좌 정보 저장
        accountRepository.save(account);

        // 7. 실패했던 출금 내역 상태 업데이트
        failedWithdrawal.setTransferStatusCode(TransferStatusCode.COMPENSATED); // 상태 변경
        transferRepository.save(failedWithdrawal);

        log.info("[보상] 서비스 처리 완료");
    }
}

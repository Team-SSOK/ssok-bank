package kr.ssok.bank.domain.transfer.service;

import kr.ssok.bank.common.comm.CommunicationProtocol;
import kr.ssok.bank.common.constant.FailureStatusCode;
import kr.ssok.bank.common.constant.TransferTypeCode;
import kr.ssok.bank.common.exception.BaseException;
import kr.ssok.bank.common.util.AESUtil;
import kr.ssok.bank.domain.account.entity.Account;
import kr.ssok.bank.domain.account.repository.AccountRepository;
import kr.ssok.bank.domain.transfer.dto.TransferDepositRequestDTO;
import kr.ssok.bank.domain.transfer.dto.TransferWithdrawRequestDTO;
import kr.ssok.bank.domain.transfer.entity.TransferHistory;
import kr.ssok.bank.domain.transfer.repository.TransferRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.SendTo;
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
    public void withdraw(TransferWithdrawRequestDTO dto) {
        // 0. 중복 transactionId 방지
        if (transferRepository.existsByTransactionIdAndTransferTypeCode(dto.getTransactionId(), TransferTypeCode.WITHDRAW)) {
            throw new BaseException(FailureStatusCode.DUPLICATED_TRANSACTION_ID);
        }

        // 1. 출금 계좌 락 걸고 조회
        // 1-1. 암호화: 출금 계좌번호
        log.info("dto.getWithdrawAccount() : " + dto.getWithdrawAccount());
        String encrypted = aesUtil.encrypt(dto.getWithdrawAccount());
        log.info("encrypted : " + encrypted);

        Account withdrawAccount = accountRepository.findWithPessimisticLockByAccountNumber(encrypted)
                .orElseThrow(() -> new BaseException(FailureStatusCode.ACCOUNT_NOT_FOUND));

        // 2. 출금 가능 여부 확인 및 처리
        if (withdrawAccount.getBalance() < dto.getTransferAmount()) {
            throw new BaseException(FailureStatusCode.TRANSFER_NO_BALANCE);
        }
        withdrawAccount.withdraw(dto.getTransferAmount());

        // 3. 출금 내역 기록
        // 3-1. 암호화: 상대 계좌 (입금 계좌)
        String encryptedCounterAccount = aesUtil.encrypt(dto.getCounterAccount());

        TransferHistory history = TransferHistory.builder()
                .account(withdrawAccount)
                .counterpartAccount(encryptedCounterAccount) // 암호화해서 저장
                .transferAmount(dto.getTransferAmount())  // 송금 금액 기록
                .balanceAfter(withdrawAccount.getBalance())  // 출금 후 잔액 기록
                .transferTypeCode(TransferTypeCode.WITHDRAW)
                .currencyCode(dto.getCurrencyCode())
                .transactionId(dto.getTransactionId())
                .build();
        transferRepository.save(history);

        // 4. 변경된 계좌 저장
        accountRepository.save(withdrawAccount);
    }

    // 입금 이체
    @Transactional
    public void deposit(TransferDepositRequestDTO dto) {
        // 0. 중복 transactionId 방지
        if (transferRepository.existsByTransactionIdAndTransferTypeCode(dto.getTransactionId(), TransferTypeCode.DEPOSIT)) {
            throw new BaseException(FailureStatusCode.DUPLICATED_TRANSACTION_ID);
        }

        // 1. 선출금 내역 존재 확인
        boolean withdrawExists = transferRepository.existsByTransactionIdAndTransferTypeCode(dto.getTransactionId(), TransferTypeCode.WITHDRAW);
        if (!withdrawExists) {
            throw new BaseException(FailureStatusCode.MISSING_WITHDRAW_FOR_DEPOSIT);
        }

        // 2. 입금 계좌 락 걸고 조회
        // 2-1. 암호화: 입금 계좌번호
        log.info("dto.getDepositAccount() : " + dto.getDepositAccount());
        String encrypted = aesUtil.encrypt(dto.getDepositAccount());
        log.info("encrypted : " + encrypted);

        Account depositAccount = accountRepository.findWithPessimisticLockByAccountNumber(encrypted)
                .orElseThrow(() -> new BaseException(FailureStatusCode.ACCOUNT_NOT_FOUND));

        // 3. 입금 처리
        depositAccount.deposit(dto.getTransferAmount());

        // 4. 입금 내역 기록
        // 4-1. 암호화: 상대 계좌 (출금 계좌)
        String encryptedCounterAccount = aesUtil.encrypt(dto.getCounterAccount());

        TransferHistory history = TransferHistory.builder()
                .account(depositAccount)
                .counterpartAccount(encryptedCounterAccount)  // 암호화해서 저장
                .transferAmount(dto.getTransferAmount())  // 송금 금액 기록
                .balanceAfter(depositAccount.getBalance())  // 입금 후 잔액 기록
                .transferTypeCode(TransferTypeCode.DEPOSIT)
                .currencyCode(dto.getCurrencyCode())
                .transactionId(dto.getTransactionId())
                .build();

        transferRepository.save(history);

        // 5. 변경된 계좌 저장
        accountRepository.save(depositAccount);
    }
}

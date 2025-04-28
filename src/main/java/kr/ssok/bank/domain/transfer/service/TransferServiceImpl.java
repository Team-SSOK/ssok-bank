package kr.ssok.bank.domain.transfer.service;

import kr.ssok.bank.common.constant.FailureStatusCode;
import kr.ssok.bank.common.constant.TransferTypeCode;
import kr.ssok.bank.common.exception.BaseException;
import kr.ssok.bank.domain.account.entity.Account;
import kr.ssok.bank.domain.account.repository.AccountRepository;
import kr.ssok.bank.domain.transfer.dto.TransferDepositRequestDTO;
import kr.ssok.bank.domain.transfer.dto.TransferWithdrawRequestDTO;
import kr.ssok.bank.domain.transfer.entity.TransferHistory;
import kr.ssok.bank.domain.transfer.repository.TransferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

@Service
@RequiredArgsConstructor
public class TransferServiceImpl {

    private Map<String, ReentrantLock> lockMap = new HashMap<>();

    private final AccountRepository accountRepository;
    private final TransferRepository transferRepository;

    // 출금 이체
    @Transactional
    public void withdraw(TransferWithdrawRequestDTO dto) {
        // 1. 출금 계좌 락 걸고 조회
        Account withdrawAccount = accountRepository.findWithPessimisticLockByAccountNumber(dto.getWithdrawAccount())
                .orElseThrow(() -> new BaseException(FailureStatusCode.ACCOUNT_NOT_FOUND));

        // 2. 출금 가능 여부 확인 및 처리
        if (withdrawAccount.getBalance() < dto.getTransferAmount()) {
            throw new BaseException(FailureStatusCode.TRANSFER_NO_BALANCE);
        }
        withdrawAccount.withdraw(dto.getTransferAmount());

        // 3. 출금 내역 기록
        TransferHistory history = TransferHistory.builder()
                .account(withdrawAccount)
                .counterpartAccount(dto.getCounterAccount())
                .transferAmount(dto.getTransferAmount())  // 송금 금액 기록
                .balanceAfter(withdrawAccount.getBalance())  // 출금 후 잔액 기록
                .transferTypeCode(TransferTypeCode.WITHDRAW)
                .build();
        transferRepository.save(history);

        // 4. 변경된 계좌 저장
        accountRepository.save(withdrawAccount);
    }



    // 입금 이체
    @Transactional
    public void deposit(TransferDepositRequestDTO dto) {
        // 1. 입금 계좌 락 걸고 조회
        /*
        if(this.lockMap.containsKey(dto.getTransactionId()))
        {

        }
        else
        {
            Object o = new Object();
            lockMap.put(dto.getTransactionId(),o);

            synchronized (o)
            {
                o.wait();

                o.notifyAll();
            }

        }*/

        Account depositAccount = accountRepository.findWithPessimisticLockByAccountNumber(dto.getDepositAccount())
                .orElseThrow(() -> new BaseException(FailureStatusCode.ACCOUNT_NOT_FOUND));

        // 2. 입금 처리
        depositAccount.deposit(dto.getTransferAmount());

        // 3. 입금 내역 기록
        TransferHistory history = TransferHistory.builder()
                .account(depositAccount)
                .counterpartAccount(dto.getCounterAccount())
                .transferAmount(dto.getTransferAmount())  // 송금 금액 기록
                .balanceAfter(depositAccount.getBalance())  // 입금 후 잔액 기록
                .transferTypeCode(TransferTypeCode.DEPOSIT)
                .build();
        transferRepository.save(history);

        // 4. 변경된 계좌 저장
        accountRepository.save(depositAccount);
    }
}

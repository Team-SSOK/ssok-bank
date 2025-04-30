package kr.ssok.bank.domain.transfer.service;

import kr.ssok.bank.common.comm.CommunicationProtocol;
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
import lombok.extern.slf4j.Slf4j;
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

    /**
     * 프로미스 요청에 대한 카프카 리스너
     * 요청한 내용을 확인후 응답을 반환합니다.
     * (kafkaListenerReplyContainerFactory 사용)
     *
     * @param request       DTO 객체
     * @param key           식별자 키
     * @param replyTopic    응답해야하는 토픽
     * @param correlationId 상관 ID
     * @return
     */
    @KafkaListener(topics = "${spring.kafka.request-topic}", groupId = "request-server-group", containerFactory = "kafkaListenerReplyContainerFactory")
    @SendTo // 응답은 헤더에 지정된 reply topic으로 전송됨
    public Object handleTransferRequest(Object request,
                                                  @Header(KafkaHeaders.RECEIVED_KEY) String key,
                                                  @Header(KafkaHeaders.REPLY_TOPIC) byte[] replyTopic,
                                                  @Header(KafkaHeaders.CORRELATION_ID) byte[] correlationId) {

        log.info("Received transfer request in bank service: {}", request);
        log.info("Correlation ID: {}", new String(correlationId));
        log.info("Reply topic: {}", replyTopic);
        log.info("Reply KEY: {}", key);

        switch (key) {
            case CommunicationProtocol.SEND_TEST_MESSAGE:
                log.info("Called SEND_TEST_MESSAGE!");
                break;
            case CommunicationProtocol.REQUEST_DEPOSIT:
                log.info("Called REQUEST_DEPOSIT!");
                break;
            case CommunicationProtocol.REQUEST_WITHDRAW:
                log.info("Called REQUEST_WITHDRAW!");
                break;
        }

        return new Object(); //Resonse DTO를 응답으로 보내야됨 (수정 필요)
    }

    /**
     * 단방향 메세지 요청에 대한 카프카 리스너
     * (kafkaListenerUnidirectionalContainerFactory 사용)
     *
     * @param key   식별자 키
     * @param value DTO 객체
     */
    @KafkaListener(topics = "${spring.kafka.push-topic}", containerFactory = "kafkaListenerUnidirectionalContainerFactory")
    public void receiveMessage(@Header(KafkaHeaders.RECEIVED_KEY) String key, Object value) {
        log.info("Received unidirectional message in bank service: {}", value);
        log.info("Received KEY: {}", key);

        switch (key) {
            // 실제 은행 송금 처리 로직 구현 (여기서는 간단히 시뮬레이션)
            case CommunicationProtocol.SEND_TEST_MESSAGE:
                log.info("Hello World!");
                break;
            case CommunicationProtocol.REQUEST_DEPOSIT:
                log.info("Hello World!!");
                break;
            case CommunicationProtocol.REQUEST_WITHDRAW:
                log.info("Hello World!!!");
                break;
        }

    }

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
                .currencyCode(dto.getCurrencyCode())
                .transactionId(dto.getTransactionId())
                .build();

        transferRepository.save(history);

        // 4. 변경된 계좌 저장
        accountRepository.save(depositAccount);
    }
}

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

    /**
     * 프로미스 요청에 대한 카프카 리스너
     * 요청한 내용을 확인후 응답을 반환합니다.
     * (kafkaListenerReplyContainerFactory 사용)
     *
     * @param record        레코드
     * @param replyTopic    응답을 보내는 토픽
     * @param correlationId 상관 ID
     * @param cmd           통신 프로토콜
     * @return
     */
    @KafkaListener(topics = "${spring.kafka.request-topic}", groupId = "request-server-group", containerFactory = "kafkaListenerReplyContainerFactory")
    @SendTo // 응답은 헤더에 지정된 replyTopic으로 전송됨
    public Object handleTransferRequest(ConsumerRecord<String, Object> record,
                                        @Header(KafkaHeaders.REPLY_TOPIC) String replyTopic,
                                        @Header(KafkaHeaders.CORRELATION_ID) String correlationId,
                                        @Header(value = "CMD", required = false) String cmd) {

        log.info("Received TransferRequest in bank service: {}", record.value());
        log.info("Received CMD: {}", cmd);
        log.info("Correlation ID: {}", correlationId);
        log.info("Reply topic: {}", replyTopic);

        // 레코드에서 record.value()를 DTO 타입으로 캐스팅하여 사용할 것

//        ModelMapper mapper = new ModelMapper();
//        TransferRequest request = mapper.map(record.value(), TransferRequest.class);
//        TransferResponse response = processTransferInBank(request);
//        if (cmd == null) return response;
//        switch (cmd) {
//            case CommunicationProtocol.SEND_TEST_MESSAGE:
//                log.info("Called SEND_TEST_MESSAGE!");
//                break;
//            case CommunicationProtocol.REQUEST_DEPOSIT:
//                log.info("Called REQUEST_DEPOSIT!");
//                break;
//            case CommunicationProtocol.REQUEST_WITHDRAW:
//                log.info("Called REQUEST_WITHDRAW!");
//                break;
//        }
//
//        log.info("Transfer processed, sending response: {}", response);

        Object response = null;
        return response;
    }

    /**
     * 단방향 메세지 요청에 대한 카프카 리스너
     * (kafkaListenerUnidirectionalContainerFactory 사용)
     *
     * @param cmd    통신 프로토콜
     * @param record 레코드
     */
    @KafkaListener(topics = "${spring.kafka.push-topic}", containerFactory = "kafkaListenerUnidirectionalContainerFactory")
    public void receiveMessage(@Header(value = "CMD", required = false) String cmd,
                               ConsumerRecord<String, Object> record) {
        log.info("Received unidirectional message in bank service: {}", record.value());
        log.info("Received CMD: {}", cmd);

        if (cmd == null) return;
        switch (cmd) {
            // 로그 확인
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
    }

    // 출금 이체
    @Transactional
    public void withdraw(TransferWithdrawRequestDTO dto) {
        // 1. 출금 계좌 락 걸고 조회
        // 1-1. 복호화: 출금 계좌번호
        String decryptedWithdrawAccount = aesUtil.decrypt(dto.getWithdrawAccount());
        Account withdrawAccount = accountRepository.findWithPessimisticLockByAccountNumber(decryptedWithdrawAccount)
                .orElseThrow(() -> new BaseException(FailureStatusCode.ACCOUNT_NOT_FOUND));

        // 2. 출금 가능 여부 확인 및 처리
        if (withdrawAccount.getBalance() < dto.getTransferAmount()) {
            throw new BaseException(FailureStatusCode.TRANSFER_NO_BALANCE);
        }
        withdrawAccount.withdraw(dto.getTransferAmount());

        // 3. 출금 내역 기록
        // 3-1. 복호화: 상대 계좌 (입금 계좌)
        String decryptedCounterAccount = aesUtil.decrypt(dto.getCounterAccount());

        TransferHistory history = TransferHistory.builder()
                .account(withdrawAccount)
                .counterpartAccount(aesUtil.encrypt(decryptedCounterAccount)) // 암호화해서 저장
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
        // 1-1. 복호화: 입금 계좌번호
        String decryptedDepositAccount = aesUtil.decrypt(dto.getDepositAccount());
        Account depositAccount = accountRepository.findWithPessimisticLockByAccountNumber(decryptedDepositAccount)
                .orElseThrow(() -> new BaseException(FailureStatusCode.ACCOUNT_NOT_FOUND));

        // 2. 입금 처리
        depositAccount.deposit(dto.getTransferAmount());

        // 3. 입금 내역 기록
        // 3-1. 복호화: 상대 계좌 (출금 계좌)
        String decryptedCounterAccount = aesUtil.decrypt(dto.getCounterAccount());
        TransferHistory history = TransferHistory.builder()
                .account(depositAccount)
                .counterpartAccount(aesUtil.encrypt(decryptedCounterAccount))  // 암호화해서 저장
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

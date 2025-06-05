package kr.ssok.bank.domain.transfer.listener;

import kr.ssok.bank.common.comm.CommunicationProtocol;
import kr.ssok.bank.common.comm.JsonUtil;
import kr.ssok.bank.common.constant.FailureStatusCode;
import kr.ssok.bank.common.constant.SuccessStatusCode;
import kr.ssok.bank.common.exception.BaseException;
import kr.ssok.bank.common.response.ApiResponse;
import kr.ssok.bank.domain.transfer.dto.CompensateRequestDTO;
import kr.ssok.bank.domain.transfer.dto.TransferDepositRequestDTO;
import kr.ssok.bank.domain.transfer.dto.TransferWithdrawRequestDTO;
import kr.ssok.bank.domain.transfer.service.TransferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransferListener {

    @Value("${spring.kafka.request-topic-dlt}")
    private String deadLetterTopic;
    private final TransferService transferService;
    private final KafkaTemplate<String, Object> replyTemplate;

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
    public String handleTransferRequest(ConsumerRecord<String, String> record,
                                        @Header(KafkaHeaders.REPLY_TOPIC) String replyTopic,
                                        @Header(KafkaHeaders.CORRELATION_ID) String correlationId,
                                        @Header(value = "CMD", required = false) String cmd) {

        log.info("Received TransferRequest in bank service: {}", record.value());
        log.info("Received CMD: {}", cmd);
        log.info("Correlation ID: {}", correlationId);
        log.info("Reply topic: {}", replyTopic);

        if (cmd == null) {
            log.info("Transfer ERROR : {}", record);
            ApiResponse<String> response = ApiResponse.of(FailureStatusCode._INTERNAL_SERVER_ERROR, null);
            return JsonUtil.toJson(response);
        }
        try {
            long messageCreatedAt = record.timestamp(); // 프로듀서가 메시지를 생성한 시간 (CreateTime)

            switch (cmd) {
                case CommunicationProtocol.REQUEST_WITHDRAW: // 출금
                    log.info("REQUEST_WITHDRAW: {}", record);

                    TransferWithdrawRequestDTO withdrawDTO = JsonUtil.fromJson(record.value(), TransferWithdrawRequestDTO.class);

                    if (isExpired(messageCreatedAt)) {
                        log.warn("Expired withdraw message. Skipping process. transactionId: {}", withdrawDTO.getTransactionId());
                        return ApiResponse.ofJson(FailureStatusCode.REQUEST_TIMEOUT, null);
                    }

                    transferService.withdraw(withdrawDTO);

                    return ApiResponse.ofJson(SuccessStatusCode.TRANSFER_WITHDRAW_OK, null);

                case CommunicationProtocol.REQUEST_DEPOSIT: // 입금
                    log.info("REQUEST_DEPOSIT : {}", record);

                    TransferDepositRequestDTO depositDTO = JsonUtil.fromJson(record.value(), TransferDepositRequestDTO.class);

                    if (isExpired(messageCreatedAt)) {
                        log.warn("Expired deposit message. Skipping process. transactionId: {}", depositDTO.getTransactionId());
                        return ApiResponse.ofJson(FailureStatusCode.REQUEST_TIMEOUT, null);
                    }

                    transferService.deposit(depositDTO);

                    return ApiResponse.ofJson(SuccessStatusCode.TRANSFER_DEPOSIT_OK, null);

                case CommunicationProtocol.REQUEST_COMPENSATE: // 보상
                    log.info("REQUEST_COMPENSATE : {}", record);
                    CompensateRequestDTO compensateDTO = JsonUtil.fromJson(record.value(), CompensateRequestDTO.class);
                    try
                    {
                        transferService.compensate(compensateDTO);
                    }
                    catch (Exception e)
                    {
                        log.error("COMPENSATE ERROR : Database Failed => Send Message to DLQ(DeadLetterQueue)");
                        this.sendToDeadLetterQueue(record, cmd, e);
                        return ApiResponse.ofJson(FailureStatusCode.TRANSFER_COMPENSATE_FAILED, null);
                    }
                    return ApiResponse.ofJson(SuccessStatusCode.TRANSFER_COMPENSATE_OK, null);
            }
        } catch (BaseException e) {
            log.error(e.getMessage(), e);
            return ApiResponse.ofJson(e.getStatus(), null);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ApiResponse.ofJson(FailureStatusCode._INTERNAL_SERVER_ERROR, null);
        }
        return ApiResponse.ofJson(FailureStatusCode._INTERNAL_SERVER_ERROR, null);
    }

    @KafkaListener(topics = "${spring.kafka.request-topic-dlt}", groupId = "request-server-group")
    public void handleFailures(ConsumerRecord<String, String> record,
                               @Header(value = "CMD", required = false) String cmd) {
        log.info("DLQ REQUEST_COMPENSATE START : {}", record.value());
        try
        {
            CompensateRequestDTO compensateDTO = JsonUtil.fromJson(record.value(), CompensateRequestDTO.class);
            transferService.compensate(compensateDTO);
            log.info("DLQ REQUEST_COMPENSATE COMPLETE - TRANSACTION ID: {}",compensateDTO.getTransactionId());
        }
        catch (Exception e)
        {
            log.error("DLQ processing failed - Manual intervention required");
            log.error("=====< FINAL COMPENSATE ERROR >=====");
            log.error("> ERROR : {}",record.value(), e);
            log.error("====================================");
        }
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
                               ConsumerRecord<String, String> record) {
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

    /**
     * 타임 아웃 확인 메서드
     * @param messageCreatedAt 메세지가 생성된 타임스탬프
     * @return
     */
    private boolean isExpired(long messageCreatedAt) {
        long currentTime = System.currentTimeMillis();
        long diff = currentTime - messageCreatedAt;
        log.info("요청 발생 후 도착까지 걸린 시간: {}ms", diff);
        return diff > 10000L; // 10초
    }

    // 해당 메서드는 재시도 요청을 하지않음, 재시도 요청은 KafkaListener에서 예외를 발생시켜야 동작
    // KafkaConfig ErrorHandler 에서 예외 라우팅 필요
    private void sendToDeadLetterQueue(ConsumerRecord<String, String> record, String cmd, Exception e) {
        // 별도 스레드에서 DLQ 처리 (응답 지연 방지)
        CompletableFuture.runAsync(() -> {
            try {
                ProducerRecord<String, Object> producer =
                        new ProducerRecord<>(deadLetterTopic, record.key(), record.value());
                producer.headers().add("CMD", cmd.getBytes(StandardCharsets.UTF_8));
                // DLQ로 메시지 전송
                replyTemplate.send(producer);
                log.info("Message sent to DLQ: {}", record);
            } catch (Exception dlqException) {
                log.error("Failed to send message to DLQ", dlqException);
            }
        });
    }

}

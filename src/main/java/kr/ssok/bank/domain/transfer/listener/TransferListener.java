package kr.ssok.bank.domain.transfer.listener;

import kr.ssok.bank.common.comm.CommunicationProtocol;
import kr.ssok.bank.common.constant.FailureStatusCode;
import kr.ssok.bank.common.constant.SuccessStatusCode;
import kr.ssok.bank.common.exception.BaseException;
import kr.ssok.bank.common.response.ApiResponse;
import kr.ssok.bank.domain.transfer.dto.TransferDepositRequestDTO;
import kr.ssok.bank.domain.transfer.dto.TransferWithdrawRequestDTO;
import kr.ssok.bank.domain.transfer.service.TransferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.modelmapper.ModelMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransferListener {

    private final TransferService transferService;
    private final ModelMapper mapper = new ModelMapper();

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

        if (cmd == null) {
            log.info("Transfer ERROR : {}", record);
            ApiResponse<String> response = ApiResponse.of(FailureStatusCode._INTERNAL_SERVER_ERROR, null);
            return response;
        }
        try {
            switch (cmd) {
                case CommunicationProtocol.REQUEST_DEPOSIT:
                    log.info("REQUEST_DEPOSIT : {}", record);

                    TransferDepositRequestDTO depositDTO = mapper.map(record.value(), TransferDepositRequestDTO.class);
                    transferService.deposit(depositDTO);

                    return ApiResponse.of(SuccessStatusCode.TRANSFER_DEPOSIT_OK, null);

                case CommunicationProtocol.REQUEST_WITHDRAW:
                    log.info("REQUEST_WITHDRAW : {}", record);

                    TransferWithdrawRequestDTO withdrawDTO = mapper.map(record.value(), TransferWithdrawRequestDTO.class);
                    transferService.withdraw(withdrawDTO);

                    return ApiResponse.of(SuccessStatusCode.TRANSFER_WITHDRAW_OK, null);
            }
        } catch (BaseException e) {
            return ApiResponse.of(e.getStatus(), null);
        } catch (Exception e) {
            return ApiResponse.of(FailureStatusCode._INTERNAL_SERVER_ERROR, null);
        }
        return ApiResponse.of(FailureStatusCode._INTERNAL_SERVER_ERROR, null);

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

}

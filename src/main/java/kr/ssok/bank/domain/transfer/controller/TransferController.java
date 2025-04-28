package kr.ssok.bank.domain.transfer.controller;

import jakarta.servlet.http.HttpServletRequest;
import kr.ssok.bank.common.constant.FailureStatusCode;
import kr.ssok.bank.common.constant.SuccessStatusCode;
import kr.ssok.bank.common.constant.UserTypeCode;
import kr.ssok.bank.common.exception.BaseException;
import kr.ssok.bank.common.response.ApiResponse;
import kr.ssok.bank.domain.account.dto.AccountRequestDTO;
import kr.ssok.bank.domain.account.entity.Account;
import kr.ssok.bank.domain.account.service.AccountService;
import kr.ssok.bank.domain.transfer.dto.TransferDepositRequestDTO;
import kr.ssok.bank.domain.transfer.dto.TransferWithdrawRequestDTO;
import kr.ssok.bank.domain.transfer.service.TransferService;
import kr.ssok.bank.domain.user.dto.UserRequestDTO;
import kr.ssok.bank.domain.user.entity.User;
import kr.ssok.bank.domain.user.repository.UserRepository;
import kr.ssok.bank.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/bank/transfer")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;

    // 출금 이체 API
    @PostMapping("/withdraw")
    public ApiResponse<String> createWithdraw(@RequestBody TransferWithdrawRequestDTO transferWithdrawRequestDTO) {
        try {
            log.info("[WITHDRAW] 요청 수신: {}", transferWithdrawRequestDTO);
            transferService.withdraw(transferWithdrawRequestDTO);
            log.info("[WITHDRAW] 출금 완료 - 계좌: {}, 금액: {}",
                    transferWithdrawRequestDTO.getWithdrawAccount(),
                    transferWithdrawRequestDTO.getTransferAmount()
            );
            return ApiResponse.of(SuccessStatusCode.TRANSFER_WITHDRAW_OK, null);
        } catch (BaseException e) {
            log.warn("[WITHDRAW] 출금 실패 - 계좌: {}, 사유: {}",
                    transferWithdrawRequestDTO.getWithdrawAccount(),
                    e.getStatus().getMessage()
            );
            throw e;
        } catch (Exception e) {
            log.error("[WITHDRAW] 서버 오류 - {}", e.getMessage(), e);
            throw new BaseException(FailureStatusCode._INTERNAL_SERVER_ERROR);
        }
    }

    // 입금 이체 API
    @PostMapping("/deposit")
    public ApiResponse<String> createDeposit(@RequestBody TransferDepositRequestDTO transferDepositRequestDTO) {
        try {
            log.info("[DEPOSIT] 요청 수신: {}", transferDepositRequestDTO);
            transferService.deposit(transferDepositRequestDTO);
            log.info("[DEPOSIT] 입금 완료 - 계좌: {}, 금액: {}",
                    transferDepositRequestDTO.getDepositAccount(),
                    transferDepositRequestDTO.getTransferAmount()
            );
            return ApiResponse.of(SuccessStatusCode.TRANSFER_DEPOSIT_OK, null);
        } catch (BaseException e) {
            log.warn("[DEPOSIT] 입금 실패 - 계좌: {}, 사유: {}",
                    transferDepositRequestDTO.getDepositAccount(),
                    e.getStatus().getMessage()
            );
            throw e;
        } catch (Exception e) {
            log.error("[DEPOSIT] 서버 오류 - {}", e.getMessage(), e);
            throw new BaseException(FailureStatusCode._INTERNAL_SERVER_ERROR);
        }
    }
}

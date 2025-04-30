package kr.ssok.bank.domain.account.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import kr.ssok.bank.common.constant.FailureStatusCode;
import kr.ssok.bank.common.constant.SuccessStatusCode;
import kr.ssok.bank.common.constant.UserTypeCode;
import kr.ssok.bank.common.exception.BaseException;
import kr.ssok.bank.common.response.ApiResponse;
import kr.ssok.bank.domain.account.dto.*;
import kr.ssok.bank.domain.account.entity.Account;
import kr.ssok.bank.domain.account.service.AccountService;
import kr.ssok.bank.domain.transfer.entity.TransferHistory;
import kr.ssok.bank.domain.user.dto.UserRequestDTO;
import kr.ssok.bank.domain.user.entity.User;
import kr.ssok.bank.domain.user.repository.UserRepository;
import kr.ssok.bank.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/bank")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final UserRepository userRepository;
    private final UserService userService;

    @Operation(summary = "예금주명 조회", description = "요청받은 계좌번호의 예금주명을 조회 합니다.")
    @PostMapping("/account/owner")
    public ApiResponse<AccountOwnerCheckResponseDTO> getUserAccounts(@RequestBody AccountOwnerCheckRequestDTO dto)
    {
        log.info("[POST] /account/owner - 예금주명 조회: accountNumber = {}", dto.getAccount());
        Account account = this.accountService.getAccountByAccountNumber(dto.getAccount());

        //해당 계좌번호의 계좌가 존재하는지 확인
        if(account != null)
        {
            User user = account.getUser();
            //해당 계좌의 사용자가 존재하는지 확인
            if (user != null)
            {
                AccountOwnerCheckResponseDTO res = AccountOwnerCheckResponseDTO.builder()
                        .username(user.getUsername())
                        .build();

                log.info("[예금주명 조회] 조회 성공. account = {}, username = {}", dto.getAccount(), user.getUsername());
                //성공 응답
                return ApiResponse.of(SuccessStatusCode.ACCOUNT_OWNER_CHECK_OK,res);
            }
            else
            {
                log.error("[예금주명 조회] 해당 계좌의 사용자가 존재하지 않습니다.");
                return ApiResponse.of(FailureStatusCode.ACCOUNT_OWNER_CHECK_FAILED,null);
            }

        }
        else
        {
            log.error("[예금주명 조회] 요청한 계좌번호는 존재 하지 않습니다. account = {}", dto.getAccount());
            return ApiResponse.of(FailureStatusCode.ACCOUNT_OWNER_CHECK_FAILED,null);
        }
    }

    @Operation(summary = "계좌 유효성 검사", description = "계좌번호와 예금주 실명번호를 요청받아 해당 계좌의 유효성을 확인합니다.")
    @PostMapping("/account/valid")
    public ApiResponse<AccountValidRequestDTO> checkAccountValidation(@RequestBody AccountValidRequestDTO dto)
    {
        log.info("[POST] /account/valid - 계좌 유효성 검사: username = {}, accountNumber = {}",dto.getUsername(), dto.getAccount());
        try
        {
            Account account = this.accountService.getAccountByAccountNumber(dto.getAccount());
            Optional<User> userOpt = Optional.ofNullable(account.getUser());
            //해당 계좌의 사용자가 존재하는지 확인
            if (userOpt.isPresent() && dto.getAccount().equals(account.getAccountNumber()) && dto.getUsername().equals(userOpt.get().getUsername())) {
                log.info("[계좌 유효성 검사] 계좌 유효성 확인 성공. account = {}, username = {}", dto.getAccount(), userOpt.get().getUsername());
                return ApiResponse.of(SuccessStatusCode.ACCOUNT_VALIDATION_OK,null);
            }
            else {
                log.error("[계좌 유효성 검사] 예금주와 계좌 정보가 일치하지 않습니다.");
                return ApiResponse.of(FailureStatusCode.ACCOUNT_VALIDATION_FAILED,null);
            }
        }
        catch (BaseException e)
        {
            log.error("[계좌 유효성 검사] 해당 계좌는 존재하지 않습니다.");
            return ApiResponse.of(FailureStatusCode.ACCOUNT_NOT_FOUND,null);
        }
    }

    @Operation(summary = "계좌 잔액 및 송금 한도 검사", description = "계좌에서 송금 처리가 가능한지를 확인합니다. (잔액 부족, 출금 한도 확인)")
    @PostMapping("/account/transferable")
    public ApiResponse<AccountTransferableCheckResponseDTO> checkTransferableAccount(@RequestBody AccountTransferableCheckRequestDTO dto)
    {
        log.info("[POST] /account/transferable - 계좌 잔액 및 송금 한도 검사: username = {}, accountNumber = {}, transferAmount = {}",dto.getUsername(), dto.getAccount(), dto.getTransferAmount());
        try
        {
            Account account = this.accountService.getAccountByAccountNumber(dto.getAccount());

            AccountTransferableCheckResponseDTO res = AccountTransferableCheckResponseDTO.builder()
                    .balance(account.getBalance())
                    .withdrawLimit(account.getWithdrawLimit())
                    .isTransferable(false)
                    .build();

            Optional<User> userOpt = Optional.ofNullable(account.getUser());

            if(userOpt.isEmpty() || !dto.getUsername().equals(userOpt.get().getUsername()))
            {
                log.error("[계좌 잔액 및 송금 한도 검사] 예금주와 계좌 정보가 일치하지 않습니다.");
                return ApiResponse.of(FailureStatusCode.ACCOUNT_VALIDATION_FAILED,null);
            }

            if(dto.getTransferAmount() <= account.getBalance())
            {
                if(dto.getTransferAmount() <= account.getWithdrawLimit())
                {
                    res.setTransferable(true);
                    log.info("[계좌 잔액 및 송금 한도 검사] 검사 성공. account = {}", dto.getAccount());
                    return ApiResponse.of(SuccessStatusCode.TRANSFER_AVAILABLE,res);
                }
                else
                {
                    log.error("[계좌 잔액 및 송금 한도 검사] 송금 요청량이 출금 한도를 초과하였습니다.");
                    return ApiResponse.of(FailureStatusCode.ACCOUNT_WITHDRAW_LIMIT_REACHED,res);
                }
            }
            else
            {
                log.error("[계좌 잔액 및 송금 한도 검사] 해당 계좌는 잔액이 부족합니다.");
                return ApiResponse.of(FailureStatusCode.TRANSFER_NO_BALANCE,res);
            }
        }
        catch (BaseException e)
        {
            log.error("[계좌 잔액 및 송금 한도 검사] 해당 계좌는 존재하지 않습니다.");
            return ApiResponse.of(FailureStatusCode.ACCOUNT_NOT_FOUND,null);
        }
    }

    @Operation(summary = "계좌 잔액 확인", description = "계좌에서 잔액을 확인합니다.")
    @PostMapping("/account/balance")
    public ApiResponse<AccountBalanceResponseDTO> checkAccountBalance(@RequestBody AccountBalanceRequestDTO dto)
    {
        log.info("[POST] /account/balance - 계좌 잔액 확인: accountNumber = {}", dto.getAccount());
        try
        {
            Optional<Account> accountOpt = Optional.ofNullable(this.accountService.getAccountByAccountNumber(dto.getAccount()));
            if(accountOpt.isPresent()) {
                Account account = accountOpt.get();
                log.info("[계좌 잔액 확인] 계좌 잔액 조회 성공. balance = {} , account = {}", account.getBalance(), dto.getAccount());
                return ApiResponse.of(SuccessStatusCode.ACCOUNT_BALANCE_OK, AccountBalanceResponseDTO.builder().balance(account.getBalance()).build());
            }
            else {
                log.error("[계좌 잔액 확인] 계좌 잔액 조회에 실패하였습니다. account = {}",dto.getAccount());
                return ApiResponse.of(FailureStatusCode.ACCOUNT_BALANCE_FAILED,null);
            }
        }
        catch (BaseException e)
        {
            log.error("[계좌 잔액 확인] 해당 계좌는 존재하지 않습니다.");
            return ApiResponse.of(FailureStatusCode.ACCOUNT_NOT_FOUND,null);
        }
    }

    @Operation(summary = "계좌 거래 내역 조회", description = "단일 계좌에 대한 거래 내역을 조회합니다.")
    @PostMapping("/account/history")
    public ApiResponse<List<AccountTransferHistoryResponseDTO>> getTransferHistory(@RequestBody AccountTransferHistoryRequestDTO dto)
    {
        log.info("[POST] /account/history - 계좌 거래 내역 조회: accountNumber = {}", dto.getAccount());
        try
        {
            Optional<Account> accountOpt = Optional.ofNullable(this.accountService.getAccountByAccountNumber(dto.getAccount()));
            if(accountOpt.isPresent())
            {
                Account account = accountOpt.get();
                List<TransferHistory> history = account.getTransferHistories();
                List<AccountTransferHistoryResponseDTO> responseList = new ArrayList<>();

                if(!history.isEmpty())
                {
                    ModelMapper modelMapper = new ModelMapper();
                    responseList = history.stream()
                            .map(transfer -> {
                                AccountTransferHistoryResponseDTO res = modelMapper.map(transfer, AccountTransferHistoryResponseDTO.class);
                                res.setAccount(account.getAccountNumber());
                                return res;
                            })
                            .toList();
                }

                log.info("[계좌 거래 내역 조회] 거래 내역 조회 성공. account = {}", dto.getAccount());
                return ApiResponse.of(SuccessStatusCode.ACCOUNT_HISTORY_OK, responseList);
            }
            else {
                log.error("[계좌 거래 내역 조회] 내역 조회에 실패하였습니다. account = {}",dto.getAccount());
                return ApiResponse.of(FailureStatusCode.ACCOUNT_HISTORY_FAILED,null);
            }
        }
        catch (BaseException e)
        {
            log.error("[계좌 거래 내역 조회] 해당 계좌는 존재하지 않습니다.");
            return ApiResponse.of(FailureStatusCode.ACCOUNT_NOT_FOUND,null);
        }
    }

}
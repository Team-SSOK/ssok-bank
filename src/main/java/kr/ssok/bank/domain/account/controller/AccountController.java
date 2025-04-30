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
import kr.ssok.bank.domain.good.entity.Good;
import kr.ssok.bank.domain.good.repository.GoodRepository;
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
    private final GoodRepository goodRepository;
    private final UserService userService;

    @Operation(summary = "계좌 개설", description = "계좌를 개설합니다.")
    @PostMapping("/account")
    public ApiResponse<String> createAccount(HttpServletRequest request, @RequestBody AccountRequestDTO accountRequest) {
        String source = request.getHeader("X-Source"); // TODO: 협의 필요 (Service 송신 여부 확인 방식)

        try {
            log.info("[POST] /account - 계좌 개설 요청: userName = {} with phone = {} from = {}"
                    , accountRequest.getUsername(), accountRequest.getPhoneNumber(), source);

            // 1. 사용자 조회
            User user = userRepository.findByUsernameAndPhoneNumber(accountRequest.getUsername(), accountRequest.getPhoneNumber())
                    .orElseGet(() -> {
                        try {
                            UserTypeCode userTypeCode;

                            // 1-1. 사용자 생성
                            // "SSOK"에서 온 요청은 무조건 '개인' 처리
                            if ("SSOK".equals(source)) {
                                userTypeCode = UserTypeCode.INDIVIDUAL;
                            } else {
                                // 나머지는 ENUM 매핑
                                try {
                                    userTypeCode = UserTypeCode.valueOf(accountRequest.getUserTypeCode().name());
                                } catch (IllegalArgumentException e) {
                                    log.error("유효하지 않은 사용자 유형 코드: {}", accountRequest.getUserTypeCode(), e);
                                    throw new BaseException(FailureStatusCode.USER_TYPE_ERROR);
                                }
                            }

                            // DTO 생성 (빌더 패턴 사용)
                            UserRequestDTO userDto = UserRequestDTO.builder()
                                    .username(accountRequest.getUsername())
                                    .phoneNumber(accountRequest.getPhoneNumber())
                                    .userTypeCode(userTypeCode)
                                    .build();

                            // 사용자 생성
                            userService.createUser(userDto);

                            // 1-2. 생성된 사용자 다시 조회
                            return userRepository.findByUsernameAndPhoneNumber(
                                    accountRequest.getUsername(), accountRequest.getPhoneNumber()
                            ).orElseThrow(() -> {
                                log.error("사용자 생성 후 재조회 중 오류 발생: {} - {}", accountRequest.getUsername(), accountRequest.getPhoneNumber());
                                throw new BaseException(FailureStatusCode.USER_NOT_FOUND);
                            });
                        } catch (Exception e) {
                            log.error("사용자 생성 중 오류 발생: {} - {}", accountRequest.getUsername(), accountRequest.getPhoneNumber(), e);
                            throw new BaseException(FailureStatusCode.USER_CREATION_FAILED);
                        }
                    });

            log.info("User found or created successfully: {} - {}", user.getUsername(), user.getPhoneNumber());

            // 2. 계좌 개설
            // 2-1. 상품 조회 로직 추가 (예금/적금 기본 상품 선택 -> 우선 [기본 예금]으로 생성)
            Good good = goodRepository.findByAccountTypeCode(accountRequest.getAccountTypeCode())
                    .orElseThrow(() -> new BaseException(FailureStatusCode.GOOD_READ_FAILED));

            // 2-2. 계좌 개설
            Account account = accountService.createAccount(user, accountRequest.getAccountTypeCode(), good);

            log.info("계좌 개설 성공: {}. Account Number: {}", user.getUsername(), account.getAccountNumber());

            // 성공 응답
            return ApiResponse.of(SuccessStatusCode.ACCOUNT_CREATE_OK, null);
        } catch (BaseException e) {
            log.error("계좌 개설 실패: {}", e.getMessage(), e);

            // 실패 응답
            return ApiResponse.of(FailureStatusCode.ACCOUNT_CREATE_FAILED, null);
        } catch (Exception e) {
            log.error("계좌 개설 중 알 수 없는 오류 발생: {}", e.getMessage(), e);

            // 서버 오류 응답
            return ApiResponse.of(FailureStatusCode._INTERNAL_SERVER_ERROR, null);
        }
    }

    @Operation(summary = "계좌 조회", description = "오픈뱅킹 서버에서 계좌 조회 요청시, Account 테이블을 조회한 후 json으로 응답합니다.")
    @PostMapping("/account/search")
    public ApiResponse<List<AccountResponseDTO>> getUserAccounts(@RequestBody AccountRequestDTO accountRequest) {

        log.info("[POST] /account - 계좌 조회 요청: username = {}, phoneNumber = {}"
                , accountRequest.getUsername(), accountRequest.getPhoneNumber());

        try {
            // 1. 계좌 조회
            List<AccountResponseDTO> response
                    = accountService.getAccountsByUsernameAndPhoneNumber(accountRequest.getUsername(), accountRequest.getPhoneNumber());

            // 1-1. 조회 결과 계좌가 없는 경우
            if (response.isEmpty()) {
                log.warn("계좌 조회 성공 (계좌 없음): username = {}, phoneNumber = {}", accountRequest.getUsername(), accountRequest.getPhoneNumber());
                return ApiResponse.of(SuccessStatusCode.ACCOUNT_READ_OK , response); // 또는 notFound()도 가능
            }

            // 성공 응답
            log.info("계좌 조회 성공: username = {}, 계좌 수 = {}", accountRequest.getUsername(), response.size());
            return ApiResponse.of(SuccessStatusCode.ACCOUNT_READ_OK, response);

        } catch (BaseException e) {
            log.error("계좌 조회 실패: username = {}, 에러 = {}", accountRequest.getUsername(), e.getMessage());

            // 실패 응답
            return ApiResponse.of(FailureStatusCode.ACCOUNT_READ_FAILED, null);

        } catch (Exception e) {
            log.error("계좌 조회 중 알 수 없는 오류 발생: username = {}, 에러 = {}", accountRequest.getUsername(), e.getMessage(), e);

            //서버 오류 응답
            return ApiResponse.of(FailureStatusCode._INTERNAL_SERVER_ERROR, null);
        }
    }

    @Operation(summary = "휴면 계좌 여부 검사", description = "계좌의 휴면 여부를 검증한다.")
    @PostMapping("/account/dormant")
    public ApiResponse<Map<String, Boolean>> checkDormantAccount(@RequestBody AccountRequestDTO accountRequest) {
        log.info("[POST] /account/dormant - 휴면계좌 여부 확인 요청: accountNumber = {}", accountRequest.getAccountNumber());

        try {
            // 1. 휴면 계좌 여부 확인
            boolean isDormant = accountService.isAccountDormant(accountRequest.getAccountNumber());

            // 성공 응답
            Map<String, Boolean> result = Map.of("isDormant", isDormant);
            return ApiResponse.of(SuccessStatusCode.ACCOUNT_DORMANT_OK, result);

        } catch (BaseException e) {
            log.error("휴면계좌 조회 실패: accountNumber = {}, 에러 = {}", accountRequest.getAccountNumber(), e.getMessage());

            // 실패 응답
            return ApiResponse.of(FailureStatusCode.ACCOUNT_DORMANT_FAILED, null);

        } catch (Exception e) {
            log.error("휴면계좌 조회 중 알 수 없는 오류 발생: accountNumber = {}, 에러 = {}", accountRequest.getAccountNumber(), e.getMessage(), e);

            // 서버 오류 응답
            return ApiResponse.of(FailureStatusCode._INTERNAL_SERVER_ERROR, null);
        }
    }

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
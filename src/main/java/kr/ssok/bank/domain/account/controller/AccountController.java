package kr.ssok.bank.domain.account.controller;

import jakarta.servlet.http.HttpServletRequest;
import kr.ssok.bank.common.constant.UserTypeCode;
import kr.ssok.bank.common.response.ApiResponse;
import kr.ssok.bank.common.constant.FailureStatusCode;
import kr.ssok.bank.domain.account.dto.AccountRequestDTO;
import kr.ssok.bank.domain.account.dto.AccountResponseDTO;
import kr.ssok.bank.domain.account.entity.Account;
import kr.ssok.bank.domain.account.service.AccountService;
import kr.ssok.bank.domain.user.dto.UserRequestDTO;
import kr.ssok.bank.domain.user.entity.User;
import kr.ssok.bank.domain.user.repository.UserRepository;
import kr.ssok.bank.domain.user.service.UserService;
import kr.ssok.bank.common.exception.BaseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/bank")
public class AccountController {

    private final AccountService accountService;
    private final UserRepository userRepository;
    private final UserService userService;

    public AccountController(AccountService accountService, UserRepository userRepository, UserService userService) {
        this.accountService = accountService;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    // 계좌 생성 API
    @PostMapping("/account")
    public ResponseEntity<ApiResponse<String>> createAccount(HttpServletRequest request, @RequestBody AccountRequestDTO accountRequest) {
        String source = request.getHeader("X-Source"); // TODO: 협의 필요 (Service 송신 여부 확인 방식)

        try {
            log.info("[POST] /account - 계좌 생성 요청: userName = {} with phone = {} from = {}"
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
                                    log.error("Invalid user type: {}", accountRequest.getUserTypeCode(), e);
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
                                log.error("User not found after creation attempt: {} - {}", accountRequest.getUsername(), accountRequest.getPhoneNumber());
                                throw new BaseException(FailureStatusCode.USER_NOT_FOUND);
                            });
                        } catch (Exception e) {
                            log.error("Failed to create user: {} - {}", accountRequest.getUsername(), accountRequest.getPhoneNumber(), e);
                            throw new BaseException(FailureStatusCode.USER_CREATION_FAILED);
                        }
                    });

            log.info("User found or created successfully: {} - {}", user.getUsername(), user.getPhoneNumber());

            // 2. 계좌 생성
            Account account = accountService.createAccount(user, accountRequest.getAccountTypeCode());

            log.info("Account created successfully for user: {}. Account Number: {}", user.getUsername(), account.getAccountNumber());

            // 성공 응답
            return ResponseEntity.ok().body(ApiResponse.onSuccess("계좌 생성 완료: " + account.getAccountNumber()));
        } catch (BaseException e) {
            log.error("Error occurred during account creation: {}", e.getMessage(), e);

            // 실패 응답
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.onFailure(e.getStatus().getCode(), e.getMessage(), null));
        } catch (Exception e) {
            log.error("Unexpected error occurred: {}", e.getMessage(), e);

            // 서버 오류 응답
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.onFailure(FailureStatusCode._INTERNAL_SERVER_ERROR.getCode(), "서버 오류가 발생했습니다. 나중에 다시 시도해주세요.", null));
        }
    }

    // 사용자 별 계좌 조회 API
    @GetMapping("/account")
    public ResponseEntity<ApiResponse<List<AccountResponseDTO>>> getUserAccounts(@RequestParam String username) {
        log.info("[GET] /account - 계좌 조회 요청: username = {}", username);

        try {
            // 1. 계좌 조회
            List<AccountResponseDTO> response = accountService.getAccountsByUsername(username);

            // 1-1. 조회 결과 계좌가 없는 경우
            if (response.isEmpty()) {
                log.warn("계좌 없음: username = {}", username);
                return ResponseEntity.ok(ApiResponse.onSuccess(response)); // or return notFound?
            }

            // 성공 응답
            log.info("계좌 조회 성공: username = {}, 계좌 수 = {}", username, response.size());
            return ResponseEntity.ok(ApiResponse.onSuccess(response));

        } catch (BaseException e) {
            log.error("계좌 조회 실패: username = {}, 에러 = {}", username, e.getMessage());
            
            // 실패 응답
            return ResponseEntity
                    .status(e.getStatus().getHttpStatus())
                    .body(ApiResponse.onFailure(e.getStatus().getCode(), e.getStatus().getMessage(), null));
        } catch (Exception e) {
            log.error("계좌 조회 중 알 수 없는 오류 발생: username = {}, 에러 = {}", username, e.getMessage(), e);
            
            // 서버 오류 응답
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.onFailure("INTERNAL500", "서버 내부 오류가 발생했습니다.", null));
        }
    }


}

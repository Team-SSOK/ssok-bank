package kr.ssok.bank.domain.account.controller;

import kr.ssok.bank.domain.account.entity.Account;
import kr.ssok.bank.domain.account.service.AccountService;
import kr.ssok.bank.domain.user.entity.User;
import kr.ssok.bank.domain.user.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bank")
public class AccountController {

   /* private final AccountService accountService;
    private final UserRepository userRepository;

    // 계좌 생성 API
    @PostMapping("/create")
    public ResponseEntity<String> createAccount(@RequestBody AccountCreationRequest request) {
        // 1. username과 phoneNumber로 사용자 조회
        User user = userRepository.findByUsernameAndPhoneNumber(request.getUsername(), request.getPhoneNumber())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 2. 계좌 생성
        Account account = accountService.createAccount(user, request.getAccountTypeCode(), request.getBankCode());

        // 3. 계좌 생성 완료 응답
        return ResponseEntity.ok("계좌 생성 완료: " + account.getAccountNumber());
    }*/
}
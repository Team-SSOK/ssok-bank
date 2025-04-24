package kr.ssok.bank.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import kr.ssok.bank.domain.user.dto.UserRequestDTO;
import kr.ssok.bank.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/bank/user")
@RequiredArgsConstructor
public class UserController {

    @Autowired
    private final UserService userService;

    @Operation(summary = "사용자 생성", description = "사용자의 정보를 생성합니다.")
    @PostMapping("")
    public ResponseEntity<String> create(@RequestBody UserRequestDTO dto)
    {
        return ResponseEntity.ok("로그인 성공");
    }

}

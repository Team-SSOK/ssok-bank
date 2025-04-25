package kr.ssok.bank.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import kr.ssok.bank.common.constant.AccountTypeCode;
import kr.ssok.bank.common.constant.FailureStatusCode;
import kr.ssok.bank.common.constant.SuccessStatusCode;
import kr.ssok.bank.common.constant.UserTypeCode;
import kr.ssok.bank.common.exception.BaseException;
import kr.ssok.bank.common.response.ApiResponse;
import kr.ssok.bank.domain.account.service.AccountService;
import kr.ssok.bank.domain.user.dto.UserRequestDTO;
import kr.ssok.bank.domain.user.entity.User;
import kr.ssok.bank.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/bank/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AccountService accountService;

    @Operation(summary = "사용자 생성", description = "사용자를 생성하고 예금계좌를 같이 생성합니다. / 사용자유형코드 (1:개인), 계좌유형코드 (1:예금)")
    @PostMapping
    public ApiResponse<UserRequestDTO> createUser(@RequestBody UserRequestDTO userRequestDto, HttpServletRequest request) {
        try
        {
            User user = this.userService.createUser(userRequestDto);
            this.accountService.createAccount(user, AccountTypeCode.DEPOSIT);
            return ApiResponse.of(SuccessStatusCode.USER_CREATION_OK,null);
        }
        catch (BaseException e)
        {
            return ApiResponse.of(FailureStatusCode.USER_CREATION_FAILED,null);
        }
    }

    @Operation(summary = "테스트", description = "테스트 API")
    @PostMapping(value = "/test")
    private ResponseEntity<Object> test(@RequestBody UserRequestDTO userRequestDto) throws Exception
    {
        Map<String,Object> map = new HashMap<>();
        map.put("code","200");
        map.put("message","사용자 생성에 성공하였습니다.");
        return ResponseEntity.status(HttpStatus.OK)
                .body(map);
    }

}

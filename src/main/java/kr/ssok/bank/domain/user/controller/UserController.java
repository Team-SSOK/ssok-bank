package kr.ssok.bank.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import kr.ssok.bank.common.exception.BaseException;
import kr.ssok.bank.common.response.ApiResponse;
import kr.ssok.bank.domain.user.dto.UserRequestDTO;
import kr.ssok.bank.domain.user.entity.User;
import kr.ssok.bank.domain.user.service.UserService;
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
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService)
    {
        this.userService = userService;
    }

    @Operation(summary = "사용자 생성", description = "사용자를 생성하는 API")
    @PostMapping
    public ApiResponse<UserRequestDTO> createUser(@RequestBody UserRequestDTO userRequestDto, HttpServletRequest request) {
        try
        {
            User user = this.userService.createUser(userRequestDto);
            return ApiResponse.onSuccess("200", "사용자 생성에 성공하였습니다.", null);
        }
        catch (BaseException e)
        {
            return ApiResponse.onFailure("400", "사용자 생성에 실패하였습니다.", null);
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

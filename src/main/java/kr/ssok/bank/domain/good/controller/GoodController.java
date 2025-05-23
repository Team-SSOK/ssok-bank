package kr.ssok.bank.domain.good.controller;

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
import kr.ssok.bank.domain.good.dto.GoodResponseDTO;
import kr.ssok.bank.domain.good.service.GoodService;
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
public class GoodController {

    private final GoodService goodService;

    @Operation(summary = "상품 조회", description = "서비스에서 상품 조회 시, 당사 금융 상품 조회 결과를 응답합니다.")
    @GetMapping("/good")
    public ApiResponse<List<GoodResponseDTO>> getUserAccounts() {

        log.info("[상품 조회] 컨트롤러 진입");
        try {

            List<GoodResponseDTO> response = goodService.getAllGoods();

            // 1-1. 조회 결과 상품이 없는 경우
            if (response.isEmpty()) {
                log.warn("[상품 조회] 성공: 상품 없음");
                return ApiResponse.of(SuccessStatusCode.GOOD_READ_OK , null); // 또는 notFound()도 가능
            }

            // 성공 응답
            log.info("[상품 조회] 성공: 상품 수 = {}", response.size());
            return ApiResponse.of(SuccessStatusCode.GOOD_READ_OK, response);

        } catch (BaseException e) {
            log.error("[상품 조회] 실패: 에러 = {}", e.getMessage());

            // 실패 응답
            return ApiResponse.of(FailureStatusCode.GOOD_READ_FAILED, null);

        } catch (Exception e) {
            log.error("[상품 조회] 알 수 없는 오류 발생: 에러 = {}", e.getMessage(), e);

            //서버 오류 응답
            return ApiResponse.of(FailureStatusCode._INTERNAL_SERVER_ERROR, null);
        }
    }

}
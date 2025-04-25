package kr.ssok.bank.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import kr.ssok.bank.common.constant.FailureStatusCode;
import kr.ssok.bank.common.constant.SuccessStatusCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonPropertyOrder({"isSuccess", "code", "message", "result"})
public class ApiResponse<T> {

    @JsonProperty("isSuccess")
    private final Boolean isSuccess;
    private final String code;
    private final String message;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T result;

    // 코드 사용
    public static <T> ApiResponse<T> of(BaseCode code, T result){
        return new ApiResponse<>(code instanceof SuccessStatusCode, code.getReasonHttpStatus().getCode() , code.getReasonHttpStatus().getMessage(), result);
    }

    // 성공한 경우 응답 생성
    public static <T> ApiResponse<T> onSuccess(T result){
        return new ApiResponse<>(true, SuccessStatusCode._OK.getCode() , SuccessStatusCode._OK.getMessage(), result);
    }

    // 하드 코딩 용도
    public static <T> ApiResponse<T> onSuccess(String code, String message, T data){
        return new ApiResponse<>(true, code, message, data);
    }

    // 실패한 경우 응답 생성
    public static <T> ApiResponse<T> onFailure(T result){
        return new ApiResponse<>(false, FailureStatusCode._BAD_REQUEST.getCode() , FailureStatusCode._BAD_REQUEST.getMessage(), result);
    }

    // 하드 코딩 용도
    public static <T> ApiResponse<T> onFailure(String code, String message, T data){
        return new ApiResponse<>(false, code, message, data);
    }
}

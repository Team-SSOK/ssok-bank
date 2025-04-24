package kr.ssok.bank.domain.user.dto;

import kr.ssok.bank.common.constant.UserTypeCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserRequestDTO {
    private String username;
    private String phoneNumber;
    private String bankCode;
    private UserTypeCode userTypeCode;
}

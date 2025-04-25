package kr.ssok.bank.domain.account.dto;

import kr.ssok.bank.common.constant.AccountTypeCode;
import kr.ssok.bank.common.constant.BankCode;
import kr.ssok.bank.common.constant.UserTypeCode;
import lombok.Getter;

@Getter
public class AccountRequestDTO {
    private String username; // 사용자 이름
    private String phoneNumber; // 사용자 전화번호
    private AccountTypeCode accountTypeCode; // 계좌 유형 코드
    private UserTypeCode userTypeCode; //사용자 유형 코드
    private BankCode bankCode; // 은행 코드
}

package kr.ssok.bank.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserTypeCode {

    //개인
    INDIVIDUAL(1,"INDIVIDUAL"),
    //사업자
    BUSINESS_OWNER(2,"BUSINESS_OWNER"),
    //기업
    CORPORATION(3,"CORPORATION");

    private final int idx;
    private final String value;

}

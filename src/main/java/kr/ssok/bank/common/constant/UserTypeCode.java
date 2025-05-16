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
    //법인
    CORPORATION(3,"CORPORATION");

    private final int idx;
    private final String value;

    public static UserTypeCode valueOf(int input) {
        switch (input) {
            case 1: return INDIVIDUAL;
            case 2: return BUSINESS_OWNER;
            case 3: return CORPORATION;
            default: return null;
        }
    }

}

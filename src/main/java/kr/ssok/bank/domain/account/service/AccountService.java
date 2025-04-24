package kr.ssok.bank.domain.account.service;

import kr.ssok.bank.common.constant.AccountTypeCode;
import kr.ssok.bank.common.constant.BankCode;
import kr.ssok.bank.domain.account.entity.Account;
import kr.ssok.bank.domain.user.entity.User;

public interface AccountService {
    public Account createAccount(User user, BankCode bankCode, AccountTypeCode accountTypeCode);
}

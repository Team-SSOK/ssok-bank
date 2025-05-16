package kr.ssok.bank.domain.account.service;

import kr.ssok.bank.common.constant.AccountTypeCode;
import kr.ssok.bank.common.exception.BaseException;
import kr.ssok.bank.domain.account.dto.AccountResponseDTO;
import kr.ssok.bank.domain.account.entity.Account;
import kr.ssok.bank.domain.good.entity.Good;
import kr.ssok.bank.domain.user.entity.User;

import java.util.List;

public interface AccountService {
    public List<Account> createAccount(User user, AccountTypeCode accountTypeCode, Good good) throws BaseException;
    public Account getAccountByAccountNumber(String accountNumber);
    public List<AccountResponseDTO> getAccountsByUsernameAndPhoneNumber(String username, String phoneNumber);
    public boolean isAccountDormant(String accountNumber);
}

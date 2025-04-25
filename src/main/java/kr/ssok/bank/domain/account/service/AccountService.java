package kr.ssok.bank.domain.account.service;

import kr.ssok.bank.common.constant.AccountTypeCode;
import kr.ssok.bank.common.constant.BankCode;
import kr.ssok.bank.domain.account.dto.AccountResponseDTO;
import kr.ssok.bank.domain.account.entity.Account;
import kr.ssok.bank.domain.user.entity.User;

import java.util.List;

public interface AccountService {
    public Account createAccount(User user, AccountTypeCode accountTypeCode);
    public List<AccountResponseDTO> getAccountsByUsernameAndPhoneNumber(String username, String phoneNumber);
    public boolean isAccountDormant(String accountNumber);
}

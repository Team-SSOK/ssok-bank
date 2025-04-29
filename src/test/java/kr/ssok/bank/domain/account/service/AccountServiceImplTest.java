package kr.ssok.bank.domain.account.service;

import kr.ssok.bank.common.constant.AccountStatusCode;
import kr.ssok.bank.common.constant.AccountTypeCode;
import kr.ssok.bank.common.constant.BankCode;
import kr.ssok.bank.common.constant.FailureStatusCode;
import kr.ssok.bank.common.exception.BaseException;
import kr.ssok.bank.domain.account.dto.AccountResponseDTO;
import kr.ssok.bank.domain.account.entity.Account;
import kr.ssok.bank.domain.account.repository.AccountRepository;
import kr.ssok.bank.domain.user.entity.User;
import kr.ssok.bank.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountServiceImpl accountService;

    private User testUser;
    private Account testAccount;

    @BeforeEach
    void setUp() {
        // 테스트용 사용자 생성
        testUser = User.builder()
                .userId(1L)
                .username("testuser")
                .phoneNumber("01012345678")
                .build();

        // 테스트용 계좌 생성
        testAccount = Account.builder()
                .accountId(1L)
                .accountTypeCode(AccountTypeCode.SAVINGS)
                .accountNumber("626-01-1234-123456")
                .balance(10000L)
                .bankCode(BankCode.SSOK_BANK)
                .accountStatusCode(AccountStatusCode.ACTIVE)
                .withdrawLimit(300000L)
                .user(testUser)
                .build();
    }

    @Test
    @DisplayName("계좌 생성 테스트 - 성공")
    void testCreateAccount_Success() {
        // Given
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        // When
        Account result = accountService.createAccount(testUser, AccountTypeCode.SAVINGS);

        // Then
        assertNotNull(result);
        assertEquals(testAccount.getAccountId(), result.getAccountId());
        assertEquals(testAccount.getAccountTypeCode(), result.getAccountTypeCode());
        assertEquals(testAccount.getBankCode(), result.getBankCode());
        assertEquals(testAccount.getAccountStatusCode(), result.getAccountStatusCode());
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    @DisplayName("계좌 생성 테스트 - 실패 (예외 발생)")
    void testCreateAccount_Failure() {
        // Given
        when(accountRepository.save(any(Account.class))).thenThrow(new RuntimeException("Database error"));

        // When & Then
        BaseException exception = assertThrows(BaseException.class, () -> {
            accountService.createAccount(testUser, AccountTypeCode.SAVINGS);
        });

        assertEquals(FailureStatusCode.ACCOUNT_CREATE_FAILED, FailureStatusCode.ACCOUNT_CREATE_FAILED);
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    @DisplayName("계좌번호로 계좌 조회 테스트 - 성공")
    void testGetAccountByAccountNumber_Success() {
        // Given
        String accountNumber = "626-01-1234-123456";
        when(accountRepository.findAccountByAccountNumber(accountNumber)).thenReturn(Optional.of(testAccount));

        // When
        Account result = accountService.getAccountByAccountNumber(accountNumber);

        // Then
        assertNotNull(result);
        assertEquals(testAccount.getAccountId(), result.getAccountId());
        assertEquals(testAccount.getAccountNumber(), result.getAccountNumber());
        verify(accountRepository, times(1)).findAccountByAccountNumber(accountNumber);
    }

    @Test
    @DisplayName("계좌번호로 계좌 조회 테스트 - 실패 (계좌 없음)")
    void testGetAccountByAccountNumber_NotFound() {
        // Given
        String accountNumber = "non-existent-account";
        when(accountRepository.findAccountByAccountNumber(accountNumber)).thenReturn(Optional.empty());

        // When & Then
        BaseException exception = assertThrows(BaseException.class, () -> {
            accountService.getAccountByAccountNumber(accountNumber);
        });

        assertEquals(FailureStatusCode.ACCOUNT_NOT_FOUND, FailureStatusCode.ACCOUNT_NOT_FOUND);
        verify(accountRepository, times(1)).findAccountByAccountNumber(accountNumber);
    }

    @Test
    @DisplayName("사용자명과 전화번호로 계좌 목록 조회 테스트 - 성공")
    void testGetAccountsByUsernameAndPhoneNumber_Success() {
        // Given
        String username = "testuser";
        String phoneNumber = "01012345678";

        Account testAccount2 = Account.builder()
                .accountId(2L)
                .accountTypeCode(AccountTypeCode.DEPOSIT)
                .accountNumber("626-02-5678-789012")
                .balance(20000L)
                .bankCode(BankCode.SSOK_BANK)
                .accountStatusCode(AccountStatusCode.ACTIVE)
                .withdrawLimit(300000L)
                .user(testUser)
                .build();

        List<Account> accounts = Arrays.asList(testAccount, testAccount2);

        when(userRepository.findByUsernameAndPhoneNumber(username, phoneNumber)).thenReturn(Optional.of(testUser));
        when(accountRepository.findAllByUser(testUser)).thenReturn(accounts);

        // When
        List<AccountResponseDTO> result = accountService.getAccountsByUsernameAndPhoneNumber(username, phoneNumber);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(testAccount.getAccountNumber(), result.get(0).getAccountNumber());
        assertEquals(testAccount.getBalance(), result.get(0).getBalance());
        assertEquals(testAccount2.getAccountNumber(), result.get(1).getAccountNumber());
        assertEquals(testAccount2.getBalance(), result.get(1).getBalance());

        verify(userRepository, times(1)).findByUsernameAndPhoneNumber(username, phoneNumber);
        verify(accountRepository, times(1)).findAllByUser(testUser);
    }

    @Test
    @DisplayName("사용자명과 전화번호로 계좌 목록 조회 테스트 - 실패 (사용자 없음)")
    void testGetAccountsByUsernameAndPhoneNumber_UserNotFound() {
        // Given
        String username = "nonexistentuser";
        String phoneNumber = "01012345678";

        when(userRepository.findByUsernameAndPhoneNumber(username, phoneNumber)).thenReturn(Optional.empty());

        // When & Then
        BaseException exception = assertThrows(BaseException.class, () -> {
            accountService.getAccountsByUsernameAndPhoneNumber(username, phoneNumber);
        });

        assertEquals(FailureStatusCode.USER_NOT_FOUND, FailureStatusCode.USER_NOT_FOUND);
        verify(userRepository, times(1)).findByUsernameAndPhoneNumber(username, phoneNumber);
        verify(accountRepository, never()).findAllByUser(any(User.class));
    }

    @Test
    @DisplayName("휴면 계좌 여부 확인 테스트 - 활성 계좌")
    void testIsAccountDormant_Active() {
        // Given
        String accountNumber = "626-01-1234-123456";
        testAccount.setAccountStatusCode(AccountStatusCode.ACTIVE);

        when(accountRepository.findAccountByAccountNumber(accountNumber)).thenReturn(Optional.of(testAccount));

        // When
        boolean result = accountService.isAccountDormant(accountNumber);

        // Then
        assertFalse(result);
        verify(accountRepository, times(1)).findAccountByAccountNumber(accountNumber);
    }

    @Test
    @DisplayName("휴면 계좌 여부 확인 테스트 - 휴면 계좌")
    void testIsAccountDormant_Dormant() {
        // Given
        String accountNumber = "626-01-1234-123456";
        testAccount.setAccountStatusCode(AccountStatusCode.DORMANT);

        when(accountRepository.findAccountByAccountNumber(accountNumber)).thenReturn(Optional.of(testAccount));

        // When
        boolean result = accountService.isAccountDormant(accountNumber);

        // Then
        assertTrue(result);
        verify(accountRepository, times(1)).findAccountByAccountNumber(accountNumber);
    }
}

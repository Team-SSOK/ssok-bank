package kr.ssok.bank.domain.transfer.service;

import kr.ssok.bank.common.constant.BankCode;
import kr.ssok.bank.common.constant.CurrencyCode;
import kr.ssok.bank.common.constant.FailureStatusCode;
import kr.ssok.bank.common.exception.BaseException;
import kr.ssok.bank.domain.account.entity.Account;
import kr.ssok.bank.domain.account.repository.AccountRepository;
import kr.ssok.model.TransferDepositRequestDTO;
import kr.ssok.model.TransferWithdrawRequestDTO;
import kr.ssok.bank.domain.transfer.entity.TransferHistory;
import kr.ssok.bank.domain.transfer.repository.TransferRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.util.ReflectionTestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@EnableJpaAuditing
@ExtendWith(MockitoExtension.class)
class TransferServiceImplTest {

    @InjectMocks
    private TransferServiceImpl transferService;  // TransferServiceImpl 클래스를 주입받아 테스트

    @Mock
    private AccountRepository accountRepository;  // AccountRepository를 모킹하여 테스트

    @Mock
    private TransferRepository transferRepository;  // TransferRepository를 모킹하여 테스트

    private Account testAccount;  // 테스트용 계좌 객체

    @BeforeEach
    void setUp() {
        // 테스트용 계좌 초기화 (계좌번호: 1234567890, 잔액: 10,000)
        testAccount = Account.builder()
                .accountNumber("1234567890")
                .balance(10_000L)
                .build();
    }

    @Test
    @DisplayName("출금 성공 시 잔액이 차감되고 TransferHistory와 계좌 정보가 저장된다.")
    void withdraw_success() {
        // given
        TransferWithdrawRequestDTO dto = new TransferWithdrawRequestDTO();

        // 리플렉션을 사용해 private 필드 설정 (출금 계좌, 상대 계좌, 출금 금액 등)
        ReflectionTestUtils.setField(dto, "withdrawAccount", "1234567890");
        ReflectionTestUtils.setField(dto, "counterAccount", "9876543210");
        ReflectionTestUtils.setField(dto, "transferAmount", 5_000L);
        ReflectionTestUtils.setField(dto, "currencyCode", CurrencyCode.WON);
        ReflectionTestUtils.setField(dto, "transactionId", "tx123");
        ReflectionTestUtils.setField(dto, "withdrawBankCode", BankCode.SSOK_BANK);
        ReflectionTestUtils.setField(dto, "counterBankCode", BankCode.KAKAO_BANK);

        // 계좌를 Pessimistic Lock으로 조회
        when(accountRepository.findWithPessimisticLockByAccountNumber("1234567890"))
                .thenReturn(Optional.of(testAccount));

        // when
        transferService.withdraw(dto);  // 출금 요청 메서드 호출

        // then
        assertThat(testAccount.getBalance()).isEqualTo(5_000L);  // 출금 후 잔액 확인
        verify(transferRepository, times(1)).save(any(TransferHistory.class));  // TransferHistory 저장 호출 확인
        verify(accountRepository, times(1)).save(testAccount);  // 계좌 정보 저장 호출 확인
    }

    @Test
    @DisplayName("출금 시 잔액이 부족하면 TRANSFER_NO_BALANCE 예외가 발생한다.")
    void withdraw_fail_insufficient_balance() {
        // given
        TransferWithdrawRequestDTO dto = new TransferWithdrawRequestDTO();
        // 출금 금액이 잔액을 초과하는 요청 설정
        ReflectionTestUtils.setField(dto, "withdrawAccount", "1234567890");
        ReflectionTestUtils.setField(dto, "counterAccount", "9876543210");
        ReflectionTestUtils.setField(dto, "transferAmount", 20_000L);  // 잔액보다 큰 출금 금액
        ReflectionTestUtils.setField(dto, "currencyCode", CurrencyCode.WON);
        ReflectionTestUtils.setField(dto, "transactionId", "tx123");
        ReflectionTestUtils.setField(dto, "withdrawBankCode", BankCode.SSOK_BANK);
        ReflectionTestUtils.setField(dto, "counterBankCode", BankCode.KAKAO_BANK);

        when(accountRepository.findWithPessimisticLockByAccountNumber("1234567890"))
                .thenReturn(Optional.of(testAccount));

        // when & then
        BaseException exception = catchThrowableOfType(() -> transferService.withdraw(dto), BaseException.class);  // 예외가 발생하는지 확인
        assertThat(exception.getStatus()).isEqualTo(FailureStatusCode.TRANSFER_NO_BALANCE);  // 실패 상태 코드 확인

        verify(transferRepository, never()).save(any());  // TransferHistory 저장 호출되지 않아야 함
        verify(accountRepository, never()).save(any());  // 계좌 저장 호출되지 않아야 함
    }

    @Test
    @DisplayName("출금 시 계좌가 존재하지 않으면 ACCOUNT_NOT_FOUND 예외가 발생한다.")
    void withdraw_fail_account_not_found() {
        // given
        TransferWithdrawRequestDTO dto = new TransferWithdrawRequestDTO();
        // 존재하지 않는 계좌 번호 설정
        ReflectionTestUtils.setField(dto, "withdrawAccount", "0000000000");
        ReflectionTestUtils.setField(dto, "counterAccount", "9876543210");
        ReflectionTestUtils.setField(dto, "transferAmount", 1_000L);
        ReflectionTestUtils.setField(dto, "currencyCode", CurrencyCode.WON);
        ReflectionTestUtils.setField(dto, "transactionId", "tx404");
        ReflectionTestUtils.setField(dto, "withdrawBankCode", BankCode.SSOK_BANK);
        ReflectionTestUtils.setField(dto, "counterBankCode", BankCode.KAKAO_BANK);

        // 존재하지 않는 계좌 조회
        when(accountRepository.findWithPessimisticLockByAccountNumber("0000000000"))
                .thenReturn(Optional.empty());

        // when & then
        BaseException exception = catchThrowableOfType(() -> transferService.withdraw(dto), BaseException.class);  // 예외가 발생하는지 확인
        assertThat(exception.getStatus()).isEqualTo(FailureStatusCode.ACCOUNT_NOT_FOUND);  // 실패 상태 코드 확인
    }

    @Test
    @DisplayName("입금 성공 시 잔액이 증가하고 TransferHistory와 계좌 정보가 저장된다.")
    void deposit_success() {
        // given
        TransferDepositRequestDTO dto = new TransferDepositRequestDTO();
        // 리플렉션을 사용해 private 필드 설정 (입금 계좌, 상대 계좌, 입금 금액 등)
        ReflectionTestUtils.setField(dto, "depositAccount", "1234567890");
        ReflectionTestUtils.setField(dto, "counterAccount", "1112223333");
        ReflectionTestUtils.setField(dto, "transferAmount", 3_000L);
        ReflectionTestUtils.setField(dto, "currencyCode", CurrencyCode.WON);
        ReflectionTestUtils.setField(dto, "transactionId", "tx789");
        ReflectionTestUtils.setField(dto, "depositBankCode", BankCode.SSOK_BANK);
        ReflectionTestUtils.setField(dto, "counterBankCode", BankCode.KAKAO_BANK);

        // 계좌를 Pessimistic Lock으로 조회
        when(accountRepository.findWithPessimisticLockByAccountNumber("1234567890"))
                .thenReturn(Optional.of(testAccount));

        // when
        transferService.deposit(dto);  // 입금 요청 메서드 호출

        // then
        assertThat(testAccount.getBalance()).isEqualTo(13_000L);  // 입금 후 잔액 확인
        verify(transferRepository, times(1)).save(any(TransferHistory.class));  // TransferHistory 저장 호출 확인
        verify(accountRepository, times(1)).save(testAccount);  // 계좌 정보 저장 호출 확인
    }

    @Test
    @DisplayName("입금 시 계좌가 존재하지 않으면 ACCOUNT_NOT_FOUND 예외가 발생한다.")
    void deposit_fail_account_not_found() {
        // given
        TransferDepositRequestDTO dto = new TransferDepositRequestDTO();
        // 존재하지 않는 계좌 번호 설정
        ReflectionTestUtils.setField(dto, "depositAccount", "0000000000");
        ReflectionTestUtils.setField(dto, "counterAccount", "1112223333");
        ReflectionTestUtils.setField(dto, "transferAmount", 3_000L);
        ReflectionTestUtils.setField(dto, "currencyCode", CurrencyCode.WON);
        ReflectionTestUtils.setField(dto, "transactionId", "tx789");
        ReflectionTestUtils.setField(dto, "depositBankCode", BankCode.SSOK_BANK);
        ReflectionTestUtils.setField(dto, "counterBankCode", BankCode.KAKAO_BANK);

        // 존재하지 않는 계좌 조회
        when(accountRepository.findWithPessimisticLockByAccountNumber("0000000000"))
                .thenReturn(Optional.empty());

        // when & then
        BaseException exception = catchThrowableOfType(() -> transferService.deposit(dto), BaseException.class);  // 예외가 발생하는지 확인
        assertThat(exception.getStatus()).isEqualTo(FailureStatusCode.ACCOUNT_NOT_FOUND);  // 실패 상태 코드 확인
    }
}

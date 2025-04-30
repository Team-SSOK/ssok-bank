package kr.ssok.bank.domain.account.repository;

import jakarta.persistence.LockModeType;
import kr.ssok.bank.common.constant.AccountStatusCode;
import kr.ssok.bank.domain.account.entity.Account;
import kr.ssok.bank.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findAllByUser(User user);
    boolean existsByAccountNumber(String accountNumber);
    Optional<Account> findAccountByAccountNumber(String accountNumber);
    List<Account> findByAccountStatusCodeAndBalanceGreaterThan(AccountStatusCode statusCode, Long minBalance);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.accountNumber = :accountNumber")
    Optional<Account> findWithPessimisticLockByAccountNumber(@Param("accountNumber") String accountNumber);

}

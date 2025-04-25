package kr.ssok.bank.domain.account.repository;

import kr.ssok.bank.domain.account.entity.Account;
import kr.ssok.bank.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findAllByUser(User user);
    boolean existsByAccountNumber(String accountNumber);
    Optional<Account> findAccountByAccountNumber(String accountNumber);

}

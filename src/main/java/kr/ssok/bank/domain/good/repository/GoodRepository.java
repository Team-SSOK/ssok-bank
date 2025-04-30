package kr.ssok.bank.domain.good.repository;

import kr.ssok.bank.common.constant.AccountTypeCode;
import kr.ssok.bank.domain.good.entity.Good;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface GoodRepository extends JpaRepository<Good, Long> {
    Optional<Good> findByAccountTypeCode(AccountTypeCode accountTypeCode);
}

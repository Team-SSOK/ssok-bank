package kr.ssok.bank.domain.transfer.repository;

import kr.ssok.bank.common.constant.TransferTypeCode;
import kr.ssok.bank.domain.account.entity.Account;
import kr.ssok.bank.domain.transfer.entity.TransferHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TransferRepository extends JpaRepository<TransferHistory, Long>  {
    boolean existsByTransactionIdAndTransferTypeCode(String transactionId, TransferTypeCode transferTypeCode);
    Optional<TransferHistory> findByTransactionIdAndTransferTypeCode(String transactionId, TransferTypeCode transferTypeCode);
}

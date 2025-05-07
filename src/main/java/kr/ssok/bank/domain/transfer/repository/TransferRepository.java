package kr.ssok.bank.domain.transfer.repository;

import kr.ssok.bank.common.constant.TransferTypeCode;
import kr.ssok.bank.domain.account.entity.Account;
import kr.ssok.bank.domain.transfer.entity.TransferHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransferRepository extends JpaRepository<TransferHistory, Long>  {
    boolean existsByTransactionIdAndTransferTypeCode(String transactionId, TransferTypeCode transferTypeCode);

}

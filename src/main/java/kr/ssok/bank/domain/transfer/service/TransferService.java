package kr.ssok.bank.domain.transfer.service;

import kr.ssok.bank.domain.transfer.dto.CompensateRequestDTO;
import kr.ssok.bank.domain.transfer.dto.TransferDepositRequestDTO;
import kr.ssok.bank.domain.transfer.dto.TransferWithdrawRequestDTO;

public interface TransferService {
    public void withdraw(TransferWithdrawRequestDTO transferWithdrawRequestDTO);
    public void deposit(TransferDepositRequestDTO transferDepositRequestDTO);
    public void compensate(CompensateRequestDTO compensateRequestDTO);
}

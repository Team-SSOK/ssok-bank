package kr.ssok.bank.domain.transfer.service;

import kr.ssok.model.CompensateRequestDTO;
import kr.ssok.model.TransferDepositRequestDTO;
import kr.ssok.model.TransferWithdrawRequestDTO;

public interface TransferService {
    public void withdraw(TransferWithdrawRequestDTO transferWithdrawRequestDTO);
    public void deposit(TransferDepositRequestDTO transferDepositRequestDTO);
    public void compensate(CompensateRequestDTO compensateRequestDTO);
}

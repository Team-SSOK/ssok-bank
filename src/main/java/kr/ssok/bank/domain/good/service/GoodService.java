package kr.ssok.bank.domain.good.service;

import kr.ssok.bank.common.constant.AccountTypeCode;
import kr.ssok.bank.common.exception.BaseException;
import kr.ssok.bank.domain.account.dto.AccountResponseDTO;
import kr.ssok.bank.domain.account.entity.Account;
import kr.ssok.bank.domain.good.dto.GoodResponseDTO;
import kr.ssok.bank.domain.good.dto.GoodResponseDto;
import kr.ssok.bank.domain.user.entity.User;

import java.util.List;

public interface GoodService {
    List<GoodResponseDTO> getAllGoods();
}

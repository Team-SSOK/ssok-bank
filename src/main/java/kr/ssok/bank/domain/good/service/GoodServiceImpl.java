package kr.ssok.bank.domain.good.service;

import kr.ssok.bank.common.constant.AccountStatusCode;
import kr.ssok.bank.common.constant.AccountTypeCode;
import kr.ssok.bank.common.constant.BankCode;
import kr.ssok.bank.common.constant.FailureStatusCode;
import kr.ssok.bank.common.exception.BaseException;
import kr.ssok.bank.domain.account.dto.AccountResponseDTO;
import kr.ssok.bank.domain.account.entity.Account;
import kr.ssok.bank.domain.account.repository.AccountRepository;
import kr.ssok.bank.domain.account.service.AccountService;
import kr.ssok.bank.domain.good.dto.GoodResponseDTO;
import kr.ssok.bank.domain.good.repository.GoodRepository;
import kr.ssok.bank.domain.user.entity.User;
import kr.ssok.bank.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoodServiceImpl implements GoodService {

    private final GoodRepository goodRepository;

    @Override
    public List<GoodResponseDTO> getAllGoods() {
        return goodRepository.findAll().stream()
                .map(GoodResponseDTO::from)
                .collect(Collectors.toList());
    }
}
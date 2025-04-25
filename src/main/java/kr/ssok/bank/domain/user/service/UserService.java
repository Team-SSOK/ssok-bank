package kr.ssok.bank.domain.user.service;

import kr.ssok.bank.common.exception.BaseException;
import kr.ssok.bank.domain.user.dto.UserRequestDTO;
import kr.ssok.bank.domain.user.entity.User;

public interface UserService {
    User createUser(UserRequestDTO userDto) throws BaseException;
}

package kr.ssok.bank.domain.user.service;

import kr.ssok.bank.domain.user.dto.UserRequestDTO;

public interface UserService {
    void createUser(UserRequestDTO userDto);
}

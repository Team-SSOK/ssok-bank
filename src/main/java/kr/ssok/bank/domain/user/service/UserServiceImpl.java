package kr.ssok.bank.domain.user.service;

import kr.ssok.bank.domain.user.dto.UserRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{

    @Override
    public void createUser(UserRequestDTO userDto) {

    }
}

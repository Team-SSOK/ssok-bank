package kr.ssok.bank.domain.user.service;

import kr.ssok.bank.common.exception.BaseException;
import kr.ssok.bank.common.response.code.status.ErrorStatusCode;
import kr.ssok.bank.domain.user.dto.UserRequestDTO;
import kr.ssok.bank.domain.user.entity.User;
import kr.ssok.bank.domain.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository)
    {
        this.userRepository = userRepository;
    }

    @Override
    public void createUser(UserRequestDTO userDto) throws BaseException {

        this.userRepository.findByUsername(userDto.getUsername()).ifPresent(user -> {
            throw new BaseException(ErrorStatusCode.USER_ALREADY_EXISTS);
        });

        User user = User.builder().username(userDto.getUsername())
                .phoneNumber(userDto.getPhoneNumber())
                .userTypeCode(userDto.getUserTypeCode())
                .build();

        this.userRepository.save(user);

    }
}

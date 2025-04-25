package kr.ssok.bank.domain.user.service;

import kr.ssok.bank.common.exception.BaseException;
import kr.ssok.bank.common.constant.FailureStatusCode;
import kr.ssok.bank.domain.user.dto.UserRequestDTO;
import kr.ssok.bank.domain.user.entity.User;
import kr.ssok.bank.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;

    @Override
    public User createUser(UserRequestDTO userDto) throws BaseException {

        this.userRepository.findByUsernameAndPhoneNumber(userDto.getUsername(),userDto.getPhoneNumber()).ifPresent(user -> {
            throw new BaseException(FailureStatusCode.USER_ALREADY_EXISTS);
        });

        User user = User.builder().username(userDto.getUsername())
                .phoneNumber(userDto.getPhoneNumber())
                .userTypeCode(userDto.getUserTypeCode())
                .dailyTransactionTotal(0L)
                .build();

        this.userRepository.save(user);

        return user;
    }
}

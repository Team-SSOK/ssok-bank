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
        log.info("[사용자 생성] 서비스 진입: 사용자 이름 = {}", userDto.getUsername());

        this.userRepository.findByPhoneNumber(userDto.getPhoneNumber()).ifPresent(existingUser -> {
            // 동일한 번호로 가입된 사용자가 있는데 이름이 다를 경우 예외 처리
            if (!existingUser.getUsername().equals(userDto.getUsername())) {
                throw new BaseException(FailureStatusCode.PHONE_ALREADY_USED_BY_DIFFERENT_NAME);
            }
            // 동일한 이름+번호 조합이면 기존 회원
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

package kr.ssok.bank.domain.user.service;

import kr.ssok.bank.common.constant.FailureStatusCode;
import kr.ssok.bank.common.constant.UserTypeCode;
import kr.ssok.bank.common.exception.BaseException;
import kr.ssok.bank.domain.user.dto.UserRequestDTO;
import kr.ssok.bank.domain.user.entity.User;
import kr.ssok.bank.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@EnableJpaAuditing
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @InjectMocks
    private UserServiceImpl userService;  // UserServiceImpl 클래스를 주입받아 테스트

    @Mock
    private UserRepository userRepository;  // UserRepository를 모킹하여 테스트

    private UserRequestDTO userRequestDTO;  // 테스트용 UserRequestDTO 객체

    @BeforeEach
    void setUp() {
        // UserRequestDTO 초기화 (사용자 이름, 전화번호, 사용자 타입 설정)
        userRequestDTO = UserRequestDTO.builder()
                .username("JohnDoe")
                .phoneNumber("01012345678")
                .userTypeCode(UserTypeCode.INDIVIDUAL)
                .build();
    }

    @Test
    @DisplayName("사용자 생성 성공 시, 사용자 정보가 올바르게 반환된다.")
    void createUser_success() {
        // given
        // 동일한 사용자 이름과 전화번호를 가진 사용자가 없다고 설정
        when(userRepository.findByUsernameAndPhoneNumber(userRequestDTO.getUsername(), userRequestDTO.getPhoneNumber()))
                .thenReturn(Optional.empty());

        // userRepository.save() 메서드는 호출된 인자를 그대로 반환하도록 설정
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));  // 저장한 User 객체 그대로 리턴

        // when
        User createdUser = userService.createUser(userRequestDTO);  // 사용자 생성 메서드 호출

        // then
        // 생성된 사용자의 정보가 입력값과 일치하는지 확인
        assertThat(createdUser.getUsername()).isEqualTo("JohnDoe");
        assertThat(createdUser.getPhoneNumber()).isEqualTo("01012345678");
        assertThat(createdUser.getUserTypeCode()).isEqualTo(UserTypeCode.INDIVIDUAL);
        assertThat(createdUser.getDailyTransactionTotal()).isEqualTo(0L);  // 기본 일일 거래 총액이 0인지 확인

        // userRepository.save()가 한 번 호출되었는지 검증
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("사용자 생성 시 이미 동일한 사용자 정보가 존재하면 예외가 발생한다.")
    void createUser_fail_user_already_exists() {
        // given
        // 동일한 사용자 이름과 전화번호를 가진 기존 사용자 생성
        User existingUser = User.builder()
                .username("JohnDoe")
                .phoneNumber("01012345678")
                .userTypeCode(UserTypeCode.INDIVIDUAL)
                .dailyTransactionTotal(0L)
                .build();

        // 사용자 정보가 이미 존재한다고 설정
        when(userRepository.findByUsernameAndPhoneNumber(userRequestDTO.getUsername(), userRequestDTO.getPhoneNumber()))
                .thenReturn(Optional.of(existingUser));

        // when & then
        // 사용자 생성 시 예외가 발생하는지 확인
        BaseException exception = catchThrowableOfType(() -> userService.createUser(userRequestDTO), BaseException.class);

        // 예외의 상태 코드가 USER_ALREADY_EXISTS인지 확인
        assertThat(exception.getStatus()).isEqualTo(FailureStatusCode.USER_ALREADY_EXISTS);

        // userRepository.save()는 호출되지 않았어야 함
        verify(userRepository, never()).save(any(User.class));
    }
}

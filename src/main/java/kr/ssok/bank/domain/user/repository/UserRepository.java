package kr.ssok.bank.domain.user.repository;

import kr.ssok.bank.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository  extends JpaRepository<User, Long> {

}

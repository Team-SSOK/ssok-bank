package kr.ssok.bank.domain.good.repository;

import kr.ssok.bank.domain.good.entity.Good;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GoodRepository extends JpaRepository<Good, Long> {
}

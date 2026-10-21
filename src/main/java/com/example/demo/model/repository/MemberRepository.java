package com.example.demo.model.repository;

import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.demo.model.domain.Member;

@Repository // 리포지토리 등록
public interface MemberRepository extends JpaRepository<Member, Long> {
    // SELECT * FROM member WHERE username = ?
    Optional<Member> findByUsername(String username);
    // 아이디 중복 확인 (존재하면 true)
    boolean existsByUsername(String username);
    // [7주차 연습문제] 아이디나 이름에 검색어가 포함된 회원
    List<Member> findByUsernameContainingOrNameContaining(String username, String name);
}

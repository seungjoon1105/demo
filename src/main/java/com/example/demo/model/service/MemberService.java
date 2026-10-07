package com.example.demo.model.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.demo.model.domain.Member;
import com.example.demo.model.dto.MemberForm;
import com.example.demo.model.repository.MemberRepository;

@Service // 서비스 등록
public class MemberService implements UserDetailsService {
    @Autowired
    private MemberRepository memberRepository;
    @Autowired // SecurityConfig 에 등록한 BCryptPasswordEncoder 주입
    private PasswordEncoder passwordEncoder;

    // [6주차 연습문제] MANAGER 권한 추가
    private static final List<String> ROLES = List.of("USER", "MANAGER", "ADMIN");

    public Member signup(MemberForm form) {
        if (memberRepository.existsByUsername(form.getUsername())) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }
        // [5주차 연습문제] 비밀번호 확인
        if (!form.getPassword().equals(form.getPasswordConfirm())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        Member member = new Member();
        member.setUsername(form.getUsername());
        member.setPassword(passwordEncoder.encode(form.getPassword()));
        member.setName(form.getName());
        member.setRole("USER");
        return memberRepository.save(member);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Member member = memberRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("회원 없음 : " + username));
        return User.builder()
            .username(member.getUsername())
            .password(member.getPassword())
            .roles(member.getRole())
            .build();
    }

    public Member findByUsername(String username) {
        return memberRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다 : " + username));
    }

    public List<Member> findAll() {
        return memberRepository.findAll(Sort.by("id"));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void changeRole(Long id, String role) {
        if (!ROLES.contains(role)) {
            throw new IllegalArgumentException("허용되지 않는 권한입니다 : " + role);
        }
        Member member = memberRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));
        // [6주차 연습문제] 관리자 본인 보호
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if (member.getUsername().equals(username)) {
            throw new IllegalArgumentException("자기 자신의 권한 변경·삭제는 할 수 없습니다.");
        }
        member.setRole(role);
        memberRepository.save(member);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteMember(Long id) {
        Member member = memberRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));
        // [6주차 연습문제] 관리자 본인 보호
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if (member.getUsername().equals(username)) {
            throw new IllegalArgumentException("자기 자신의 권한 변경·삭제는 할 수 없습니다.");
        }
        memberRepository.delete(member);
    }
}

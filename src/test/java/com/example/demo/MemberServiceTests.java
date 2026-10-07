package com.example.demo;

import com.example.demo.model.dto.MemberForm;
import com.example.demo.model.service.MemberService;
import com.example.demo.model.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.jupiter.api.Assertions.*;

@org.springframework.test.context.TestExecutionListeners(listeners = {
    org.springframework.test.context.support.DependencyInjectionTestExecutionListener.class,
    org.springframework.test.context.transaction.TransactionalTestExecutionListener.class
})
@SpringBootTest
@Transactional
class MemberServiceTests {
    @Autowired MemberService service;
    @Autowired MemberRepository repository;
    @Autowired PasswordEncoder encoder;

    private MemberForm form(String username) {
        MemberForm f = new MemberForm();
        f.setUsername(username);
        f.setPassword("123123");
        f.setPasswordConfirm("123123");
        f.setName("학생");
        return f;
    }

    @Test void signupHashesPasswordAndLoadsUserRole() {
        var member = service.signup(form("student1"));
        assertNotEquals("123123", member.getPassword());
        assertTrue(encoder.matches("123123", member.getPassword()));
        assertFalse(encoder.matches("wrong", member.getPassword()));
        assertEquals("USER", member.getRole());
        var user = service.loadUserByUsername("student1");
        assertEquals(member.getPassword(), user.getPassword());
        assertTrue(user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    @Test void mismatchDoesNotSaveMember() {
        var f = form("mismatch");
        f.setPasswordConfirm("different");
        assertEquals("비밀번호가 일치하지 않습니다.",
            assertThrows(IllegalArgumentException.class, () -> service.signup(f)).getMessage());
        assertFalse(repository.existsByUsername("mismatch"));
    }

    @Test void duplicateUsernameIsRejected() {
        service.signup(form("duplicate"));
        assertEquals("이미 사용 중인 아이디입니다.",
            assertThrows(IllegalArgumentException.class, () -> service.signup(form("duplicate"))).getMessage());
    }

    @Test void samePasswordUsesDifferentSalts() {
        var first = service.signup(form("saltfirst"));
        var second = service.signup(form("saltsecond"));
        assertEquals(60, first.getPassword().length());
        assertNotEquals(first.getPassword(), second.getPassword());
        assertTrue(encoder.matches("123123", first.getPassword()));
        assertTrue(encoder.matches("123123", second.getPassword()));
    }

    @Test void unknownUserCannotAuthenticate() {
        assertThrows(org.springframework.security.core.userdetails.UsernameNotFoundException.class,
            () -> service.loadUserByUsername("missing-user"));
    }

}

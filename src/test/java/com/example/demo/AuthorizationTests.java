package com.example.demo;

import com.example.demo.model.domain.Member;
import com.example.demo.model.repository.MemberRepository;
import com.example.demo.model.service.MemberService;
import jakarta.servlet.Filter;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@org.springframework.test.context.TestExecutionListeners(listeners = {
    org.springframework.test.context.support.DependencyInjectionTestExecutionListener.class,
    org.springframework.test.context.transaction.TransactionalTestExecutionListener.class
})
@SpringBootTest
class AuthorizationTests {
    @Autowired WebApplicationContext context;
    @Autowired @Qualifier("springSecurityFilterChain") Filter security;
    @Autowired MemberRepository repository;
    @Autowired MemberService service;
    @Autowired com.example.demo.model.repository.TestRepository testRepository;
    MockMvc mvc;
    Member admin, student;

    @BeforeEach void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context).addFilters(security).build();
        admin = member("authadmin", "ADMIN");
        student = member("authstudent", "USER");
    }
    @AfterEach void clear() { SecurityContextHolder.clearContext(); }
    Member member(String username, String role) {
        var member = repository.findByUsername(username).orElseGet(Member::new);
        member.setUsername(username); member.setName(username);
        member.setPassword("unused-test-password"); member.setRole(role);
        return repository.save(member);
    }
    MockHttpSession session(String username, String role) {
        var context = SecurityContextHolder.createEmptyContext();
        var user = User.withUsername(username).password("unused").roles(role).build();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
        var session = new MockHttpSession();
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
        return session;
    }
    String csrf(MvcResult page) throws Exception {
        var matcher = java.util.regex.Pattern.compile("name=\"_csrf\"[^>]*value=\"([^\"]+)\"")
            .matcher(page.getResponse().getContentAsString());
        assertTrue(matcher.find()); return matcher.group(1);
    }
    @Test void accessMatrixAndMenus() throws Exception {
        mvc.perform(get("/admin/members")).andExpect(redirectedUrl("/login"));
        mvc.perform(get("/mypage")).andExpect(redirectedUrl("/login"));
        for (String role : new String[]{"USER", "MANAGER", "ADMIN"}) {
            var session = session(student.getUsername(), role);
            mvc.perform(get("/admin/members").session(session))
                .andExpect(status().is(role.equals("ADMIN") ? 200 : 403));
            mvc.perform(get("/testdb").session(session))
                .andExpect(status().is(role.equals("USER") ? 403 : 200));
            var home = mvc.perform(get("/").session(session)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
            assertEquals(role.equals("ADMIN"), home.contains("href=\"/admin/members\""));
            assertEquals(!role.equals("USER"), home.contains("href=\"/testdb\""));
            assertTrue(home.contains("role-badge role-" + role));
            var own = mvc.perform(get("/mypage").param("id", admin.getId().toString()).session(session))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
            assertTrue(own.contains("authstudent")); assertFalse(own.contains("authadmin"));
        }
    }
    @Test void adminMutationsValidateInputsAndProtectSelf() throws Exception {
        var session = session(admin.getUsername(), "ADMIN");
        var page = mvc.perform(get("/admin/members").session(session)).andExpect(status().isOk()).andReturn();
        String token = csrf(page);
        String path = "/admin/members/" + student.getId();
        mvc.perform(post(path + "/role").session(session).param("role", "ADMIN")).andExpect(status().isForbidden());
        mvc.perform(post(path + "/role").session(session).param("_csrf", token).param("role", "SUPER"))
            .andExpect(redirectedUrl("/admin/members")).andExpect(flash().attributeExists("error"));
        assertEquals("USER", repository.findById(student.getId()).orElseThrow().getRole());
        mvc.perform(post(path + "/role").session(session).param("_csrf", token).param("role", "MANAGER"))
            .andExpect(redirectedUrl("/admin/members")).andExpect(flash().attributeExists("message"));
        assertEquals("MANAGER", repository.findById(student.getId()).orElseThrow().getRole());
        for (String operation : new String[]{"role", "delete"}) {
            mvc.perform(post("/admin/members/" + admin.getId() + "/" + operation).session(session)
                .param("_csrf", token).param("role", "USER"))
                .andExpect(flash().attribute("error", "자기 자신의 권한 변경·삭제는 할 수 없습니다."));
        }
        assertEquals("ADMIN", repository.findById(admin.getId()).orElseThrow().getRole());
        mvc.perform(post("/admin/members/9223372036854775807/delete").session(session).param("_csrf", token))
            .andExpect(flash().attribute("error", "회원이 존재하지 않습니다."));
        mvc.perform(post(path + "/delete").session(session).param("_csrf", token))
            .andExpect(redirectedUrl("/admin/members")).andExpect(flash().attributeExists("message"));
        assertFalse(repository.existsById(student.getId()));
    }
    @Test void nonAdminsCannotPostEvenWithValidCsrf() throws Exception {
        for (String role : new String[]{"USER", "MANAGER"}) {
            var session = session(student.getUsername(), role);
            String token = csrf(mvc.perform(get("/").session(session)).andReturn());
            for (String operation : new String[]{"role", "delete"}) {
                mvc.perform(post("/admin/members/" + student.getId() + "/" + operation).session(session)
                    .param("_csrf", token).param("role", "ADMIN")).andExpect(status().isForbidden());
            }
        }
        assertEquals("USER", repository.findById(student.getId()).orElseThrow().getRole());
    }
    @Test void methodSecurityBlocksDirectServiceCallsWithoutUrlFilter() {
        for (String role : new String[]{"USER", "MANAGER"}) {
            SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                student.getUsername(), null, AuthorityUtils.createAuthorityList("ROLE_" + role)));
            assertThrows(AccessDeniedException.class, () -> service.changeRole(student.getId(), "ADMIN"));
            assertThrows(AccessDeniedException.class, () -> service.deleteMember(student.getId()));
        }
        assertEquals("USER", repository.findById(student.getId()).orElseThrow().getRole());
    }
    @Test
    @org.springframework.transaction.annotation.Transactional
    void emptyTestDbPageDoesNotCreateRows() throws Exception {
        testRepository.deleteAll();
        mvc.perform(get("/testdb").session(session(admin.getUsername(), "ADMIN")))
            .andExpect(status().isOk());
        assertEquals(0, testRepository.count(), "PDF처럼 데이터는 MySQL에서 직접 입력합니다.");
    }

    @Test
    @org.springframework.transaction.annotation.Transactional
    void testDbExerciseStoresAndDisplaysAgeAndGender() throws Exception {
        var member = new com.example.demo.model.domain.TestDB();
        member.setName("홍길동"); member.setAge(25); member.setGender("남");
        var saved = testRepository.saveAndFlush(member);
        var html = mvc.perform(get("/testdb").session(session(admin.getUsername(), "ADMIN")))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertTrue(html.contains("<th>나이</th>"));
        assertTrue(html.contains("<th>성별</th>"));
        assertTrue(html.contains("<td>" + saved.getId() + "</td>"));
        assertTrue(html.contains("<td>홍길동</td>"));
        assertTrue(html.contains("<td>25</td>"));
        assertTrue(html.contains("<td>남</td>"));
    }

}

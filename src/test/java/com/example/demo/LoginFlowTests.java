package com.example.demo;

import jakarta.servlet.Filter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@org.springframework.test.context.TestExecutionListeners(listeners = {
    org.springframework.test.context.support.DependencyInjectionTestExecutionListener.class,
    org.springframework.test.context.transaction.TransactionalTestExecutionListener.class
})
@SpringBootTest
class LoginFlowTests {
    @Autowired WebApplicationContext context;
    @Autowired @Qualifier("springSecurityFilterChain") Filter security;
    @Autowired com.example.demo.model.repository.MemberRepository members;
    MockMvc mvc;
    @BeforeEach void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context).addFilters(security).build();
    }
    private String csrf(MvcResult page) throws Exception {
        var match = java.util.regex.Pattern.compile("name=\"_csrf\"[^>]*value=\"([^\"]+)\"")
            .matcher(page.getResponse().getContentAsString());
        assertTrue(match.find(), "폼에 CSRF 토큰이 있어야 합니다.");
        return match.group(1);
    }
    @Test void completeLoginLogoutAndRememberMeFlow() throws Exception {
        String anonymous = mvc.perform(get("/")).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertFalse(anonymous.contains("action=\"/logout\""));
        mvc.perform(get("/css/bootstrap.min.css")).andExpect(status().isOk());
        mvc.perform(get("/hello")).andExpect(status().isOk());
        mvc.perform(post("/signup")).andExpect(status().isForbidden());

        var signup = mvc.perform(get("/signup")).andExpect(status().isOk()).andReturn();
        var session = (MockHttpSession) signup.getRequest().getSession();
        mvc.perform(post("/signup").session(session).param("_csrf", csrf(signup))
                .param("username", "flowstudent").param("password", "123123")
                .param("passwordConfirm", "123123").param("name", "학생").param("role", "ADMIN"))
            .andExpect(redirectedUrl("/login?signup"));
        assertEquals("USER", members.findByUsername("flowstudent").orElseThrow().getRole());
        mvc.perform(get("/mypage").session(session)).andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login"));
        var login = mvc.perform(get("/login").session(session)).andReturn();
        mvc.perform(post("/login").session(session).param("_csrf", csrf(login))
                .param("username", "flowstudent").param("password", "wrong"))
            .andExpect(redirectedUrl("/login?error"));
        login = mvc.perform(get("/login").session(session)).andReturn();
        var success = mvc.perform(post("/login").session(session).param("_csrf", csrf(login))
                .param("username", "flowstudent").param("password", "123123").param("remember-me", "on"))
            .andExpect(status().is3xxRedirection()).andReturn();
        assertTrue(success.getResponse().getRedirectedUrl().contains("/mypage"));
        var remember = success.getResponse().getCookie("remember-me");
        assertNotNull(remember);
        assertEquals(604800, remember.getMaxAge());
        session = (MockHttpSession) success.getRequest().getSession();
        mvc.perform(get("/mypage").session(session)).andExpect(status().isOk());
        var home = mvc.perform(get("/").session(session)).andExpect(status().isOk()).andReturn();
        assertTrue(home.getResponse().getContentAsString().contains("flowstudent"));
        assertTrue(home.getResponse().getContentAsString().contains("action=\"/logout\""));
        // 세션이 사라져도 remember-me 쿠키만으로 인증을 복원합니다.
        mvc.perform(get("/mypage").cookie(remember)).andExpect(status().isOk());
        mvc.perform(post("/logout").session(session)).andExpect(status().isForbidden());
        mvc.perform(post("/logout").session(session).param("_csrf", csrf(home)).cookie(remember))
            .andExpect(redirectedUrl("/login?logout"))
            .andExpect(cookie().maxAge("remember-me", 0))
            .andExpect(cookie().maxAge("JSESSIONID", 0));
        assertTrue(session.isInvalid());
        mvc.perform(get("/mypage")).andExpect(redirectedUrl("/login"));
        var logout = mvc.perform(get("/login?logout")).andExpect(status().isOk()).andReturn();
        assertTrue(logout.getResponse().getContentAsString().contains("로그아웃 되었습니다."));
    }

    @Test void signupErrorsKeepNonSecretFieldsAndDoNotSave() throws Exception {
        var page = mvc.perform(get("/signup")).andReturn();
        var session = (MockHttpSession) page.getRequest().getSession();
        var mismatch = mvc.perform(post("/signup").session(session).param("_csrf", csrf(page))
                .param("username", "formstudent").param("name", "학생")
                .param("password", "private-password").param("passwordConfirm", "different"))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertTrue(mismatch.contains("비밀번호가 일치하지 않습니다."));
        assertTrue(mismatch.contains("value=\"formstudent\""));
        assertTrue(mismatch.contains("value=\"학생\""));
        assertFalse(mismatch.contains("private-password"));
        assertFalse(members.existsByUsername("formstudent"));
        mvc.perform(post("/signup").session(session).param("_csrf", csrf(page))
                .param("username", "formstudent").param("name", "학생")
                .param("password", "123123").param("passwordConfirm", "123123"))
            .andExpect(redirectedUrl("/login?signup"));
        var duplicate = mvc.perform(post("/signup").session(session).param("_csrf", csrf(page))
                .param("username", "formstudent").param("name", "학생")
                .param("password", "123123").param("passwordConfirm", "123123"))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertTrue(duplicate.contains("이미 사용 중인 아이디입니다."));
    }

    @Test void loginWithoutRememberMeDoesNotIssuePersistentCookie() throws Exception {
        var signup = mvc.perform(get("/signup")).andReturn();
        var session = (MockHttpSession) signup.getRequest().getSession();
        mvc.perform(post("/signup").session(session).param("_csrf", csrf(signup))
                .param("username", "sessiononly").param("name", "학생")
                .param("password", "123123").param("passwordConfirm", "123123"))
            .andExpect(redirectedUrl("/login?signup"));
        var login = mvc.perform(get("/login").session(session)).andReturn();
        mvc.perform(post("/login").session(session).param("username", "sessiononly").param("password", "123123"))
            .andExpect(status().isForbidden());
        mvc.perform(post("/login").session(session).param("_csrf", csrf(login))
                .param("username", "sessiononly").param("password", "123123"))
            .andExpect(redirectedUrl("/"))
            .andExpect(cookie().doesNotExist("remember-me"));
        // GET 요청만으로 로그아웃되지 않아야 합니다.
        mvc.perform(get("/logout").session(session));
        mvc.perform(get("/mypage").session(session)).andExpect(status().isOk());
        mvc.perform(get("/mypage")).andExpect(redirectedUrl("/login"));
    }

    @Test void publicPagesAndAssetsRemainAccessible() throws Exception {
        for (String path : new String[]{"/", "/hello", "/login", "/signup", "/detailed_web.html",
                "/css/bootstrap.min.css", "/js/bootstrap.min.js", "/fonts/bootstrap-icons.woff2",
                "/images/happy-bearded-young-man.jpg"}) {
            mvc.perform(get(path)).andExpect(status().isOk());
        }
        for (String path : new String[]{"/detailed_ai.html", "/detailed_game.html", "/detailed_security.html"}) {
            mvc.perform(get(path)).andExpect(redirectedUrl("/login"));
        }
    }
}

package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration // 스프링 설정 클래스 등록
@EnableWebSecurity // 스프링 시큐리티 활성화
@EnableMethodSecurity // [6주차] 메서드 보안 활성화
public class SecurityConfig {
    @Value("${app.security.remember-me-key}")
    private String rememberMeKey;

    @Bean // 비밀번호 암호화 객체 등록 (BCrypt 해시)
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean // 보안 필터 체인 등록
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/hello", "/detailed_web.html",
                    "/login", "/signup", "/error").permitAll()
                .requestMatchers("/css/**", "/js/**", "/images/**", "/fonts/**").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN") // [6주차] 관리자만
                .requestMatchers("/testdb").hasAnyRole("ADMIN", "MANAGER") // [6주차 연습문제]
                .anyRequest().authenticated())
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/")
                .failureUrl("/login?error")
                .permitAll())
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID", "remember-me"))
            .rememberMe(remember -> remember // [5주차 연습문제] 로그인 상태 유지
                .key(rememberMeKey)
                .tokenValiditySeconds(7 * 24 * 60 * 60));
        return http.build();
    }
}

package com.example.demo.controller;

import java.net.URI;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.demo.model.domain.Member;
import com.example.demo.model.dto.MemberForm;
import com.example.demo.model.dto.MemberResponse;
import com.example.demo.model.dto.RoleRequest;
import com.example.demo.model.service.MemberService;

@RestController // 관리자 회원 관리 API (JSON)
@RequestMapping("/api/admin/members") // 주소는 하나, 할 일은 메서드로 구분
public class AdminApiController {
    @Autowired
    MemberService memberService;

    @GetMapping // [연습문제 1] 검색어가 없으면 전체 목록
    public List<MemberResponse> list(@RequestParam(required = false) String keyword) {
        List<Member> members = memberService.search(keyword);
        return members.stream().map(MemberResponse::from).toList();
    }

    @GetMapping("/{id}") // 회원 1명 조회
    public ResponseEntity<MemberResponse> get(@PathVariable Long id) {
        return memberService.findById(id)
            .map(member -> ResponseEntity.ok(MemberResponse.from(member)))
            .orElse(ResponseEntity.notFound().build());
    }

    // [연습문제 2] 반복되는 IllegalArgumentException 처리는 ApiExceptionHandler로 이동
    @PostMapping // 회원 등록 : JSON 본문 → MemberForm
    public ResponseEntity<?> create(@RequestBody MemberForm form) {
        Member saved = memberService.signup(form);
        return ResponseEntity
            .created(URI.create("/api/admin/members/" + saved.getId()))
            .body(MemberResponse.from(saved));
    }

    @PatchMapping("/{id}/role") // 권한 변경
    public ResponseEntity<?> changeRole(@PathVariable Long id, @RequestBody RoleRequest request) {
        Member member = memberService.changeRole(id, request.role());
        return ResponseEntity.ok(MemberResponse.from(member));
    }

    @DeleteMapping("/{id}") // 회원 삭제
    public ResponseEntity<?> delete(@PathVariable Long id) {
        memberService.deleteMember(id);
        return ResponseEntity.noContent().build();
    }
}

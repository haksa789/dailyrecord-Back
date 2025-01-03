package com.dailyrecord.backend.controller;

import com.dailyrecord.backend.dto.LoginRequest;
import com.dailyrecord.backend.dto.LoginResponse;
import com.dailyrecord.backend.model.Member;
import com.dailyrecord.backend.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;

    @Autowired
    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping("/register")
    public ResponseEntity<Member> registerMember(@RequestBody Member member) {
        Member savedMember = memberService.registerMember(member);
        return ResponseEntity.ok(savedMember);
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<Member> getMemberByUsername(@PathVariable String username) {
        return memberService.findMemberByUsername(username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<Member> getMemberByEmail(@PathVariable String email) {
        return memberService.findMemberByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        String token = memberService.login(loginRequest.getEmail(), loginRequest.getPassword());

        if (token != null) {
            return ResponseEntity.ok(new LoginResponse(token)); // 성공 시 토큰 반환
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new LoginResponse("Invalid credentials"));
        }
    }

    // 회원 정보 수정
    @PutMapping("/{id}")
    public ResponseEntity<Member> updateMember(@PathVariable Long id, @RequestBody Member updatedMember) {
        Member member = memberService.updateMember(id, updatedMember);
        if (member == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(member);
    }

    // 회원 탈퇴
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMember(@PathVariable Long id) {
        boolean isDeactivated = memberService.deactivateMember(id);
        if (isDeactivated) {
            return ResponseEntity.noContent().build();  // 비활성화 성공
        }
        return ResponseEntity.notFound().build();  // 회원을 찾을 수 없음
    }

    // 회원 복구 (활성화)
    @PutMapping("/{id}/reactivate")
    public ResponseEntity<Void> reactivateMember(@PathVariable Long id) {
        boolean isReactivated = memberService.reactivateMember(id);
        if (isReactivated) {
            return ResponseEntity.noContent().build();  // 활성화 성공
        }
        return ResponseEntity.notFound().build();  // 회원을 찾을 수 없음
    }
}

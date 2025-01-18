package com.dailyrecord.backend.controller;

import com.dailyrecord.backend.dto.LoginRequest;
import com.dailyrecord.backend.model.Members;
import com.dailyrecord.backend.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;

    @Autowired
    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping("/register")
    public ResponseEntity<Members> registerMember(@RequestBody Members members) {
        Members savedMembers = memberService.registerMember(members);
        return ResponseEntity.ok(savedMembers);
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<Members> getMemberByUsername(@PathVariable String username) {
        return memberService.findMemberByUsername(username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        // 사용자의 이메일을 통해 Member 정보를 가져옵니다.
        Members member = memberService.findByEmail(userDetails.getUsername());

        if (member == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        // 필요한 사용자 정보만 반환합니다.
        Map<String, Object> response = new HashMap<>();
        response.put("id", member.getId());
        response.put("username", member.getUsername());
        response.put("email", member.getEmail());
        return ResponseEntity.ok(response);
    }


    @GetMapping("/email/{email}")
    public ResponseEntity<Members> getMemberByEmail(@PathVariable String email) {
        return memberService.findMemberByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest loginRequest) {
        String token = memberService.login(loginRequest.getEmail(), loginRequest.getPassword());

        if (token != null) {
            // HttpOnly 쿠키 생성
            ResponseCookie cookie = ResponseCookie.from("authToken", token)
                    .httpOnly(true) // JavaScript에서 접근 불가능
                    .secure(true) // HTTPS에서만 전송 (개발 환경에서는 false로 설정 가능)
                    .path("/") // 쿠키 유효 경로
                    .maxAge(7 * 24 * 60 * 60) // 쿠키 만료 시간: 7일
                    .sameSite("Strict") // Cross-Site 요청 제한
                    .build();

            // 쿠키를 응답 헤더에 추가
            return ResponseEntity.ok()
                    .header("Set-Cookie", cookie.toString())
                    .body("Login successful");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }


    // 회원 정보 수정 (JWT 인증 추가)
    @PutMapping("/{id}")
    public ResponseEntity<Members> updateMember(
            @PathVariable Long id,
            @RequestBody Members updatedMembers,
            @RequestHeader("Authorization") String token) {
        System.out.println("Received Authorization Header: " + token);
        try {
            Members members = memberService.updateMember(id, updatedMembers, token);
            return ResponseEntity.ok(members);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }
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

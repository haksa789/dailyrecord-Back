package com.dailyrecord.backend.security;

import com.dailyrecord.backend.model.Member;
import com.dailyrecord.backend.repository.MemberRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    public CustomUserDetailsService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 이메일로 사용자를 찾습니다.
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // Spring Security의 UserDetails를 반환합니다.
        return User.builder()
                .username(member.getEmail())
                .password(member.getPassword()) // 비밀번호는 암호화된 상태로 제공해야 합니다.
                .roles("USER") // 역할 부여 (필요에 따라 변경 가능)
                .build();
    }
}

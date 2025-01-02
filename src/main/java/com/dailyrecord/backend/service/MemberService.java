package com.dailyrecord.backend.service;

import com.dailyrecord.backend.model.Member;
import com.dailyrecord.backend.repository.MemberRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

@Service
public class MemberService {

    private static final Logger logger = LoggerFactory.getLogger(MemberService.class);

    private static final long TOKEN_EXPIRATION_MS = 86400000L; // 24시간

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;  // BCryptPasswordEncoder 사용


    @Value("${jwt.secret.key}")
    private String secretKey; // application.properties에서 가져옴

    @Autowired
    public MemberService(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public String generateToken(Member member) {
        logger.info("Generating JWT token for user: {}", member.getEmail());
        return Jwts.builder()
                .setSubject(member.getEmail())
                .claim("username", member.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + TOKEN_EXPIRATION_MS))
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .compact();
    }

    public Member registerMember(Member member) {
        logger.info("Registering new member with email: {}", member.getEmail());
        String encodedPassword = passwordEncoder.encode(member.getPassword());
        member.setPassword(encodedPassword);
        return memberRepository.save(member);
    }

    public Optional<Member> findMemberByUsername(String username) {
        return memberRepository.findByUsername(username);
    }

    public Optional<Member> findMemberByEmail(String email) {
        return memberRepository.findByEmail(email);
    }

    public String login(String email, String password) {
        logger.info("Attempting login for user: {}", email);
        Optional<Member> optionalMember = memberRepository.findByEmail(email);
        if (optionalMember.isPresent()) {
            Member member = optionalMember.get();
            if (passwordEncoder.matches(password, member.getPassword())) {
                logger.info("Login successful for user: {}", email);
                return generateToken(member);
            } else {
                logger.warn("Invalid password for user: {}", email);
                throw new IllegalArgumentException("Invalid password");
            }
        } else {
            logger.warn("User not found with email: {}", email);
            throw new IllegalArgumentException("User not found with email: " + email);
        }
    }

    // 회원 정보 수정
    public Member updateMember(Long id, Member updatedMember) {
        return memberRepository.findById(id).map(member -> {
            // 비밀번호 암호화
            if (updatedMember.getPassword() != null && !updatedMember.getPassword().isEmpty()) {
                updatedMember.setPassword(passwordEncoder.encode(updatedMember.getPassword()));
            }

            // 기존 정보 업데이트
            member.setUsername(updatedMember.getUsername());
            member.setEmail(updatedMember.getEmail());
            member.setPassword(updatedMember.getPassword());
            member.setUpdatedAt(LocalDateTime.now());  // 수정일시 업데이트

            return memberRepository.save(member);
        }).orElse(null);
    }

    // 회원 탈퇴 (isActive를 false로 변경)
    public boolean deactivateMember(Long id) {
        return memberRepository.findById(id).map(member -> {
            member.setIsActive(false);  // 계정 비활성화
            member.setUpdatedAt(LocalDateTime.now());  // 수정일시 업데이트
            memberRepository.save(member);
            return true;
        }).orElse(false);
    }

    // 회원 복구 (isActive를 true로 변경)
    public boolean reactivateMember(Long id) {
        return memberRepository.findById(id).map(member -> {
            member.setIsActive(true);  // 계정 활성화
            member.setUpdatedAt(LocalDateTime.now());  // 수정일시 업데이트
            memberRepository.save(member);
            return true;
        }).orElse(false);
    }
}

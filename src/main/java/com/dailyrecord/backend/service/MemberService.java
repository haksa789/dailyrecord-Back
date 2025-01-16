package com.dailyrecord.backend.service;

import com.dailyrecord.backend.model.Members;
import com.dailyrecord.backend.repository.MemberRepository;
import io.jsonwebtoken.JwtException;
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
    private PasswordEncoder passwordEncoder;

    @Value("${jwt.secret.key}")
    private String secretKey;

    // JWT 토큰 생성
    public String generateToken(Members members) {
        logger.info("Generating JWT token for user: {}", members.getEmail());
        return Jwts.builder()
                .setSubject(members.getEmail())
                .claim("username", members.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + TOKEN_EXPIRATION_MS))
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .compact();
    }

    // JWT 토큰의 Bearer 접두어 제거
    private String extractPureToken(String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            throw new IllegalArgumentException("JWT token must start with 'Bearer '");
        }
        return token.substring(7); // "Bearer " 이후의 토큰 부분만 추출
    }

    // JWT 토큰 검증
    private boolean validateToken(String token) {
        try {
            String pureToken = extractPureToken(token);
            Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(pureToken);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            logger.error("JWT validation error: {}", e.getMessage());
            return false;
        }
    }

    // JWT 토큰에서 이메일 추출
    public String getEmailFromToken(String token) {
        String pureToken = extractPureToken(token);
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(pureToken)
                .getBody()
                .getSubject();
    }
    public Optional<Members> findMemberByUsername(String username) {
        return memberRepository.findByUsername(username);
    }

    public Optional<Members> findMemberByEmail(String email) {
        return memberRepository.findByEmail(email);
    }
    // 회원 등록
    public Members registerMember(Members members) {
        logger.info("Registering new member with email: {}", members.getEmail());
        String encodedPassword = passwordEncoder.encode(members.getPassword());
        members.setPassword(encodedPassword);
        return memberRepository.save(members);
    }

    // 로그인
    public String login(String email, String password) {
        logger.info("Attempting login for user: {}", email);
        Optional<Members> optionalMember = memberRepository.findByEmail(email);
        if (optionalMember.isPresent()) {
            Members members = optionalMember.get();
            if (passwordEncoder.matches(password, members.getPassword())) {
                logger.info("Login successful for user: {}", email);
                return generateToken(members);
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
    public Members updateMember(Long id, Members updatedMembers, String token) {
        if (!validateToken(token)) {
            throw new IllegalArgumentException("Invalid JWT token");
        }

        String email = getEmailFromToken(token);

        return memberRepository.findById(id).map(member -> {
            if (!member.getEmail().equals(email)) {
                throw new SecurityException("Unauthorized to update this member");
            }
            if (updatedMembers.getUsername() != null && !updatedMembers.getUsername().isEmpty()) {
                member.setUsername(updatedMembers.getUsername());
            }
            if (updatedMembers.getEmail() != null && !updatedMembers.getEmail().isEmpty()) {
                member.setEmail(updatedMembers.getEmail());
            }
            if (updatedMembers.getPassword() != null && !updatedMembers.getPassword().isEmpty()) {
                member.setPassword(passwordEncoder.encode(updatedMembers.getPassword()));
            }
            member.setUpdatedAt(LocalDateTime.now());
            return memberRepository.save(member);
        }).orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
    }

    // 회원 탈퇴
    public boolean deactivateMember(Long id) {
        return memberRepository.findById(id).map(member -> {
            member.setIsActive(false);
            member.setUpdatedAt(LocalDateTime.now());
            memberRepository.save(member);
            return true;
        }).orElse(false);
    }

    // 회원 복구
    public boolean reactivateMember(Long id) {
        return memberRepository.findById(id).map(member -> {
            member.setIsActive(true);
            member.setUpdatedAt(LocalDateTime.now());
            memberRepository.save(member);
            return true;
        }).orElse(false);
    }
}

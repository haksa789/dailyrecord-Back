package com.dailyrecord.backend.repository;

import com.dailyrecord.backend.model.Members;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Members, Long> {
    Optional<Members> findByUsername(String username);
    Optional<Members> findByEmail(String email);
}

package com.dailyrecord.backend.repository;

import com.dailyrecord.backend.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByMemberId(Long memberId); // 특정 회원의 게시글 목록
    List<Post> findByStatus(String status); // 공개 상태 게시글 조회
}

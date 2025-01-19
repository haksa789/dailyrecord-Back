package com.dailyrecord.backend.controller;

import com.dailyrecord.backend.dto.StatusUpdateRequest;
import com.dailyrecord.backend.model.Posts;
import com.dailyrecord.backend.service.PostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    // POST /posts: 게시글 생성
    @PostMapping
    public ResponseEntity<Posts> createPost(@RequestBody Posts post) {
        return ResponseEntity.ok(postService.createPost(post));
    }

    // GET /posts/member/{memberId}: 특정 사용자의 게시글 목록 조회
    @GetMapping("/member/{memberId}")
    public ResponseEntity<List<Posts>> getPostsByMember(@PathVariable Long memberId) {
        return ResponseEntity.ok(postService.getPostsByMemberId(memberId));
    }

    // GET /posts/{id}: 특정 게시글 조회
    @GetMapping("/{id}")
    public ResponseEntity<Posts> getPostById(@PathVariable Long id) {
        return postService.getPostById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE /posts/{id}: 특정 게시글 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }

    // PATCH /posts/{id}/visibility: 게시글 공개 여부 수정
    @PatchMapping("/{postId}/visibility")
    public ResponseEntity<Map<String, String>> updatePostVisibility(
            @PathVariable Long postId,
            @RequestBody Map<String, String> requestBody) {
        String status = requestBody.get("status");
        if (status == null || status.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Status is required"));
        }

        // 서비스 계층 호출
        try {
            boolean updated = postService.updatePostStatus(postId, status);
            if (updated) {
                return ResponseEntity.ok(Map.of("message", "게시글 상태가 성공적으로 업데이트되었습니다."));
            } else {
                return ResponseEntity.status(404).body(Map.of("error", "게시글을 찾을 수 없습니다."));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "상태 업데이트 중 오류 발생: " + e.getMessage()));
        }
    }

    // GET /public/posts: 공개 게시글 목록 조회
    @GetMapping("/public")
    public ResponseEntity<List<Posts>> getPublicPosts() {
        return ResponseEntity.ok(postService.getPublicPosts());
    }
}

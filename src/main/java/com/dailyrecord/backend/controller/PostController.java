package com.dailyrecord.backend.controller;

import com.dailyrecord.backend.model.Posts;
import com.dailyrecord.backend.service.PostService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    @PatchMapping("/{id}/visibility")
    public ResponseEntity<Posts> updatePostVisibility(@PathVariable Long id, @RequestBody String status) {
        return ResponseEntity.ok(postService.updatePostVisibility(id, status));
    }

    // GET /public/posts: 공개 게시글 목록 조회
    @GetMapping("/public")
    public ResponseEntity<List<Posts>> getPublicPosts() {
        return ResponseEntity.ok(postService.getPublicPosts());
    }
}

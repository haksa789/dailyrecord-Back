package com.dailyrecord.backend.service;

import com.dailyrecord.backend.model.Posts;
import com.dailyrecord.backend.repository.PostRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PostService {

    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public Posts createPost(Posts post) {
        return postRepository.save(post);
    }

    public Optional<Posts> getPostById(Long id) {
        return postRepository.findById(id);
    }

    public List<Posts> getPostsByMemberId(Long memberId) {
        return postRepository.findByMemberId(memberId);
    }

    public void deletePost(Long id) {
        postRepository.deleteById(id);
    }

    public List<Posts> getPublicPosts() {
        return postRepository.findByStatus("published");
    }

    public Posts updatePostVisibility(Long id, String status) {
        Posts post = postRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Post not found"));
        post.setStatus(status);
        return postRepository.save(post);
    }
}

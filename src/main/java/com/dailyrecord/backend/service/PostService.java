package com.dailyrecord.backend.service;

import com.dailyrecord.backend.model.AiGenerateData;
import com.dailyrecord.backend.model.Posts;
import com.dailyrecord.backend.repository.AiGenerateDataRepository;
import com.dailyrecord.backend.repository.PostRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final AiGenerateDataRepository aiGenerateDataRepository;

    public PostService(PostRepository postRepository, AiGenerateDataRepository aiGenerateDataRepository) {
        this.postRepository = postRepository;
        this.aiGenerateDataRepository = aiGenerateDataRepository;
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

    public boolean updatePostStatus(Long postId, String status) {
        Optional<Posts> postOptional = postRepository.findById(postId);

        if (postOptional.isPresent()) {
            Posts post = postOptional.get();
            post.setStatus(status); // 상태 업데이트
            postRepository.save(post); // 저장
            return true; // 업데이트 성공
        }

        return false; // 게시글을 찾지 못한 경우
    }
}

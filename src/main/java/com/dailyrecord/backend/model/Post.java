package com.dailyrecord.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "posts")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Lob
    private String content;

    @Column(length = 20)
    private String status = "draft"; // 기본 상태: draft

    private LocalDateTime publishedAt;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member; // Member와 연관 관계

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt = LocalDateTime.now();
}

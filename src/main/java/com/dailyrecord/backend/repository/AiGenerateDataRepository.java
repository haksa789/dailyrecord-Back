package com.dailyrecord.backend.repository;

import com.dailyrecord.backend.model.AiGenerateData;
import com.dailyrecord.backend.model.Photos;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AiGenerateDataRepository extends JpaRepository<AiGenerateData, Long> {
    Optional<AiGenerateData> findByPhoto(Photos photo); // Photo로 검색
    boolean existsByPhotoId(Long photoId);
}

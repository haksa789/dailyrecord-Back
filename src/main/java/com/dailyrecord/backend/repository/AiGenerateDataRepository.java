package com.dailyrecord.backend.repository;

import com.dailyrecord.backend.model.AiGenerateData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiGenerateDataRepository extends JpaRepository<AiGenerateData, Long> {
    boolean existsByPhotoId(Long photoId);
}

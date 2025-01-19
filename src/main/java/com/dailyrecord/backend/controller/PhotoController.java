package com.dailyrecord.backend.controller;

import com.dailyrecord.backend.dto.FileUploadResponse;
import com.dailyrecord.backend.model.AiGenerateData;
import com.dailyrecord.backend.model.Photos;
import com.dailyrecord.backend.model.Members;
import com.dailyrecord.backend.model.Posts;
import com.dailyrecord.backend.repository.AiGenerateDataRepository;
import com.dailyrecord.backend.repository.PhotosRepository;
import com.dailyrecord.backend.repository.MemberRepository;
import com.dailyrecord.backend.repository.PostRepository;
import com.dailyrecord.backend.service.ExifService;
import com.dailyrecord.backend.service.OpenAiService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/photos")
public class PhotoController {

    private final String uploadDir = "D:/dailyrecord-Back/uploads";
    private final ExifService exifService;
    private final OpenAiService openAiService;
    private final PhotosRepository photosRepository;
    private final MemberRepository membersRepository;
    private final PostRepository postsRepository;
    private final AiGenerateDataRepository aiGenerateDataRepository;

    private static final Logger logger = LoggerFactory.getLogger(PhotoController.class);

    public PhotoController(ExifService exifService, OpenAiService openAiService, PhotosRepository photosRepository, MemberRepository membersRepository, PostRepository postsRepository, AiGenerateDataRepository aiGenerateDataRepository) {
        this.exifService = exifService;
        this.openAiService = openAiService;
        this.photosRepository = photosRepository;
        this.membersRepository = membersRepository;
        this.postsRepository = postsRepository;
        this.aiGenerateDataRepository = aiGenerateDataRepository;

        // 업로드 디렉토리 생성
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<FileUploadResponse> uploadPhoto(
            @RequestParam("file") MultipartFile file,
            @RequestParam("memberId") Long memberId,
            @RequestParam("postId") Long postId
    ) {
        try {
            // 멤버 및 게시글 조회
            Members member = membersRepository.findById(memberId)
                    .orElseThrow(() -> new RuntimeException("Member not found with ID: " + memberId));
            Posts post = postsRepository.findById(postId)
                    .orElseThrow(() -> new RuntimeException("Post not found with ID: " + postId));

            // 파일 저장
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            File destinationFile = new File(uploadDir, fileName);
            file.transferTo(destinationFile);

            // EXIF 데이터 추출 및 설정
            Map<String, String> exifData = exifService.extractExifData(destinationFile);
            Photos photo = new Photos();
            photo.setFileName(fileName);
            photo.setFileSize(file.getSize());

            if (exifData.get("GPS Latitude") != null) {
                photo.setLatitude(Double.valueOf(exifData.get("GPS Latitude")));
            }
            if (exifData.get("GPS Longitude") != null) {
                photo.setLongitude(Double.valueOf(exifData.get("GPS Longitude")));
            }
            if (exifData.get("TakenAt") != null) {
                try {
                    photo.setTakenAt(LocalDateTime.parse(exifData.get("TakenAt")));
                } catch (DateTimeParseException e) {
                    photo.setTakenAt(null);
                }
            }

            // 관계 설정 및 DB 저장
            photo.setMember(member);
            photo.setPost(post);
            Photos savedPhoto = photosRepository.save(photo); // 저장된 Photo 엔티티

            // 응답에 photoId 포함
            FileUploadResponse response = new FileUploadResponse(
                    "파일 업로드 성공 및 DB 저장 완료",
                    fileName,
                    savedPhoto.getId() // 저장된 Photo의 ID 반환
            );
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new FileUploadResponse("파일 업로드 실패", null, null));
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new FileUploadResponse("요청 처리 실패: " + e.getMessage(), null, null));
        }
    }
    // AI 캡션 및 스토리 생성 API
    @PostMapping("/{photoId}/analyze")
    public ResponseEntity<?> analyzePhoto(
            @PathVariable Long photoId,
            @RequestBody Map<String, String> additionalInfo // 추가 정보 받기
    ) {
        try {
            Photos photo = photosRepository.findById(photoId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 사진이 존재하지 않습니다."));

            if (aiGenerateDataRepository.existsByPhotoId(photoId)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "이미 분석 데이터가 존재합니다."));
            }

            // 추가 정보 추출
            String atmosphereofwriting = additionalInfo.getOrDefault("atmosphereofwriting", "알 수 없음");
            String place = additionalInfo.getOrDefault("place", "알 수 없음");
            String age = additionalInfo.getOrDefault("age", "알 수 없음");
            String companions = additionalInfo.getOrDefault("companions", "혼자");
            String mbti = additionalInfo.getOrDefault("mbti", "알 수 없음");
            String situation = additionalInfo.getOrDefault("situation", "상황 정보 없음");

            // OpenAI API 호출 (추가 정보 포함)
            String analysis = openAiService.analyzePhoto(photo, atmosphereofwriting, place, age, companions, mbti, situation);

            // Caption 데이터 생성
            String captionContent = String.format(
                    "글 분위기: %s\n장소: %s\n나이: %s\n동행: %s\nMBTI: %s\n상황: %s",
                    atmosphereofwriting,
                    place,
                    age,
                    companions,
                    mbti,
                    situation
            );

            // 데이터 저장
            AiGenerateData aiData = new AiGenerateData();
            aiData.setPhoto(photo);
            aiData.setStory(analysis);
            aiData.setCaption(captionContent); // Caption 추가

            aiGenerateDataRepository.save(aiData);

            return ResponseEntity.ok(Map.of(
                    "message", "사진 분석 완료",
                    "analysis", analysis,
                    "caption", captionContent
            ));
        } catch (Exception e) {
            logger.error("사진 분석 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "사진 분석 중 오류 발생", "details", e.getMessage()));
        }
    }
    @PatchMapping("/{photoId}/update-story")
    public ResponseEntity<?> updateStory(
            @PathVariable Long photoId,
            @RequestBody Map<String, String> request) {
        String story = request.get("story");
        boolean updated = openAiService.updateAIStory(photoId, story);

        if (updated) {
            return ResponseEntity.ok(Map.of("message", "AI 스토리가 성공적으로 업데이트되었습니다."));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "해당 사진 ID를 찾을 수 없습니다."));
        }
    }
}


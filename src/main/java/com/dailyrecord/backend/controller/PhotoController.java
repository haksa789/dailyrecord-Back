package com.dailyrecord.backend.controller;

import com.dailyrecord.backend.dto.FileUploadResponse;
import com.dailyrecord.backend.model.Photos;
import com.dailyrecord.backend.model.Members;
import com.dailyrecord.backend.model.Posts;
import com.dailyrecord.backend.repository.PhotosRepository;
import com.dailyrecord.backend.repository.MemberRepository;
import com.dailyrecord.backend.repository.PostRepository;
import com.dailyrecord.backend.service.ExifService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/photos")
public class PhotoController {

    private final String uploadDir = "D:/dailyrecord-Back/uploads";
    private final ExifService exifService;
    private final PhotosRepository photosRepository;
    private final MemberRepository membersRepository;
    private final PostRepository postsRepository;

    public PhotoController(ExifService exifService, PhotosRepository photosRepository, MemberRepository membersRepository, PostRepository postsRepository) {
        this.exifService = exifService;
        this.photosRepository = photosRepository;
        this.membersRepository = membersRepository;
        this.postsRepository = postsRepository;

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
            @RequestParam("postId") Long postId // postId 요청 파라미터 추가
    ) {
        try {
            // 멤버 조회
            Members member = membersRepository.findById(memberId)
                    .orElseThrow(() -> new RuntimeException("Member not found with ID: " + memberId));

            // 게시글 조회
            Posts post = postsRepository.findById(postId)
                    .orElseThrow(() -> new RuntimeException("Post not found with ID: " + postId));

            // 파일 저장
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            File destinationFile = new File(uploadDir, fileName);
            file.transferTo(destinationFile);

            // EXIF 데이터 추출
            Map<String, String> exifData = exifService.extractExifData(destinationFile);

            // Photos 엔티티 생성 및 값 설정
            Photos photo = new Photos();
            photo.setFileName(fileName);
            photo.setFileSize(file.getSize());

            // EXIF 데이터 설정
            String latitudeStr = exifData.get("GPS Latitude");
            String longitudeStr = exifData.get("GPS Longitude");
            String takenAtStr = exifData.get("TakenAt");

            // latitude, longitude가 null이면 0으로 설정하지 않고 null로 유지
            if (latitudeStr != null) {
                photo.setLatitude(Double.valueOf(latitudeStr));
            }

            if (longitudeStr != null) {
                photo.setLongitude(Double.valueOf(longitudeStr));
            }

            // taken_at 설정 (형식이 잘못된 경우 null로 설정)
            if (takenAtStr != null) {
                try {
                    photo.setTakenAt(LocalDateTime.parse(takenAtStr));
                } catch (DateTimeParseException e) {
                    photo.setTakenAt(null);
                }
            }

            // 관계 설정
            photo.setMember(member); // member 설정
            photo.setPost(post); // post 설정

            // DB에 저장
            photosRepository.save(photo);

            // JSON 응답 반환
            FileUploadResponse response = new FileUploadResponse(
                    "파일 업로드 성공 및 DB 저장 완료",
                    fileName
            );
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            e.printStackTrace();
            FileUploadResponse errorResponse = new FileUploadResponse(
                    "파일 업로드 실패",
                    null
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        } catch (RuntimeException e) {
            e.printStackTrace();
            FileUploadResponse errorResponse = new FileUploadResponse(
                    "요청 처리 실패: " + e.getMessage(),
                    null
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
}


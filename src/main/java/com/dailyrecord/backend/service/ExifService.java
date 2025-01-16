package com.dailyrecord.backend.service;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class ExifService {

    public Map<String, String> extractExifData(File imageFile) {
        Map<String, String> exifData = new HashMap<>();
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(imageFile);
            ExifSubIFDDirectory exifDir = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);

            if (exifDir != null && exifDir.getDateOriginal() != null) {
                // 날짜를 문자열로 변환
                String dateString = exifDir.getDateOriginal().toString();

                // DateTimeFormatter로 문자열 파싱
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss z yyyy", java.util.Locale.ENGLISH);
                LocalDateTime takenAt = LocalDateTime.parse(dateString, formatter);

                exifData.put("TakenAt", takenAt.toString());
            }
        } catch (Exception e) {
            System.out.println("메타데이터 추출 실패: 기본값을 반환합니다.");
        }

        // 기본값 설정
        exifData.putIfAbsent("TakenAt", "2000-01-01T00:00:00");
        exifData.putIfAbsent("GPS Latitude", "0.0");
        exifData.putIfAbsent("GPS Longitude", "0.0");

        return exifData;
    }
}
package com.dailyrecord.backend.service;

import com.dailyrecord.backend.model.Photos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OpenAiService {

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.api.endpoint}")
    private String endpoint;

    private final RestTemplate restTemplate;

    @Autowired
    public OpenAiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String analyzePhoto(Photos photo,String atmosphereofwriting, String place, String age, String companions, String mbti, String situation) {
        String prompt = String.format(
                "당신은 내 블로그 글을 대신 써주는 사람입니다. " +
                        "다음 사진에 대해 블로그 글을 작성해주세요. 아래 정보를 참고해 작성하세요. 내 블로그에 올릴 글을 사진을 보고 대신 작성해주세요.\n\n" +
                        "- 글의 분위기: %s\n" +
                        "- 대략적인 위치: %s\n" +
                        "- 사진 이름: %s\n" +
                        "- 위도: %s\n" +
                        "- 경도: %s\n" +
                        "- 나이: %s\n" +
                        "- 함께 있는 사람: %s\n" +
                        "- MBTI: %s\n" +
                        "- 상황: %s\n\n" +
                        "거짓 정보는 넣지 말고, 사용자에게 유용하고 진정성 있는 블로그 글을 작성해주세요. " +
                        "글은 매끄럽고 독자에게 흥미를 줄 수 있도록 작성하세요. 너무 딱딱하지 않은 어투로 작성하며, 자연스러운 문장을 사용하세요.",
                atmosphereofwriting,
                place,
                photo.getFileName(),
                photo.getLatitude() != null ? photo.getLatitude() : "없음",
                photo.getLongitude() != null ? photo.getLongitude() : "없음",
                age,
                companions,
                mbti,
                situation
        );

        return callOpenAiApi(prompt);
    }

    private String callOpenAiApi(String prompt) {
        String url = endpoint;

        // 요청 본문 생성
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o-mini-2024-07-18");
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", "You are an AI assistant specializing in photo metadata analysis."),
                Map.of("role", "user", "content", prompt)
        ));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        // OpenAI API 요청
        ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                Map.class
        );

        // 응답 데이터 추출
        Map<String, Object> responseBody = response.getBody();
        if (responseBody != null && responseBody.containsKey("choices")) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
            return (String) ((Map<String, Object>) choices.get(0).get("message")).get("content");
        }

        throw new RuntimeException("OpenAI API 응답 형식이 올바르지 않습니다.");
    }
}


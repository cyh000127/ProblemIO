package com.problemio.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor // JSON 파싱을 위해 기본 생성자 추가 권장
public class TokenResponse {
    private String accessToken;
    private String refreshToken;
}
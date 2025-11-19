package com.problemio.user.service;

import com.problemio.user.dto.TokenResponse;
import com.problemio.user.dto.UserLoginRequest;
import com.problemio.user.dto.UserResponse;
import com.problemio.user.dto.UserSignupRequest;

public interface UserService {

    // 회원가입
    UserResponse signup(UserSignupRequest request);

    // 로그인 (Access Token + Refresh Token 반환)
    TokenResponse login(UserLoginRequest request);

    // 토큰 재발급 (Refresh Token 사용)
    TokenResponse reissue(String refreshToken);

    // 로그아웃 (Refresh Token 삭제)
    void logout(String email);

    // 회원 탈퇴 (Soft Delete)
    void withdraw(String email, String password);
}
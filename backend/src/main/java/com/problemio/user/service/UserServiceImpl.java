package com.problemio.user.service;

import com.problemio.global.exception.BusinessException;
import com.problemio.global.exception.ErrorCode;
import com.problemio.quiz.dto.QuizSummaryDto;
import com.problemio.user.domain.User;
import com.problemio.user.dto.UserResponse;
import com.problemio.user.dto.UserSummaryDto;
import com.problemio.user.mapper.UserAuthMapper;
import com.problemio.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    // 사진 저장 위치 확인
    private static final String UPLOAD_DIR = "C:/upload/";
    
    private final UserMapper userMapper;
    private final UserAuthMapper userAuthMapper; // 비밀번호 검증용
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponse getUserById(Long id) {
        return userMapper.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    // 파일 저장 로직 분리
    private String saveFile(MultipartFile file) throws IOException {
        // 1. 디렉토리 생성 (없으면)
        File folder = new File(UPLOAD_DIR);
        if (!folder.exists()) {
            folder.mkdirs(); // 상위 폴더까지 모두 생성
        }

        // 2. 유니크한 파일명 생성 (UUID + 원본확장자)
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String savedFilename = UUID.randomUUID().toString() + extension;

        // 3. 실제 저장 (C:/upload/uuid.jpg)
        File destination = new File(UPLOAD_DIR + savedFilename);
        file.transferTo(destination);

        // 4. DB에 저장할 접근 URL 반환 (/uploads/uuid.jpg)
        // WebMvcConfig에서 매핑한 경로를 리턴해야 함
        return "/uploads/" + savedFilename;
    }

    @Transactional
    public UserResponse updateProfile(Long userId, UserResponse request, MultipartFile file) {
        // 1. 이미지가 들어왔다면 파일 저장 처리
        if (file != null && !file.isEmpty()) {
            try {
                String savedPath = saveFile(file); // 파일 저장 후 접근 URL 반환
                request.setProfileImageUrl(savedPath); // DTO에 경로 주입
            } catch (IOException e) {
                throw new RuntimeException("파일 저장 중 오류가 발생했습니다.", e);
            }
        }

        // 2. DB 업데이트
        request.setId(userId);
        userMapper.updateProfile(request);

        return getUserById(userId);
    }

    @Override
    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        //  현재 비밀번호 확인을 위해 AuthMapper 사용
        User user = userAuthMapper.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new BusinessException(ErrorCode. INVALID_LOGIN);
        }

        // 2. 새 비밀번호 암호화 및 변경
        String encodedPassword = passwordEncoder.encode(newPassword);
        userMapper.updatePassword(userId, encodedPassword);
    }

    @Override
    @Transactional
    public void deleteAccount(Long userId, String password) {
        // 비밀번호 확인
        User user = userAuthMapper.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new BusinessException(ErrorCode. INVALID_LOGIN);
        }

        //  탈퇴 처리
        userMapper.deleteUser(userId);
    }

    @Override
    public UserSummaryDto getMySummary(Long userId) {
        UserResponse user = getUserById(userId);
        return UserSummaryDto.builder()
                .userId(userId)
                .nickname(user.getNickname())
                .followerCount(userMapper.countFollowers(userId))
                .followingCount(userMapper.countFollowings(userId))
                .createdQuizCount(userMapper.countCreatedQuizzes(userId))
                .build();
    }

    @Override
    public List<QuizSummaryDto> getMyQuizzes(Long userId) {
        // return userMapper.findMyQuizzes(userId);
        return List.of();
    }

    @Override
    public List<QuizSummaryDto> getMyLikedQuizzes(Long userId) {
        // return userMapper.findMyLikedQuizzes(userId);
        return List.of();
    }

    @Override
    public List<QuizSummaryDto> getFollowingQuizzes(Long userId) {
        // return userMapper.findFollowingQuizzes(userId);
        return List.of();
    }
}
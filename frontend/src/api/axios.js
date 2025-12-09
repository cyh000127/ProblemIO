// src/api/axios.js
import axios, { AxiosHeaders } from "axios";

// API 기본 URL 설정 (환경변수로 관리 가능)
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api";

// Axios 인스턴스 생성
const apiClient = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true, // Refresh Token(쿠키) 송수신을 위해 필수
});

// 요청 인터셉터
apiClient.interceptors.request.use(
  (config) => {
    // headers 객체가 없으면 AxiosHeaders 인스턴스로 생성
    if (!config.headers) {
      config.headers = new AxiosHeaders();
    }

    // 토큰 추가
    const token = localStorage.getItem("accessToken");
    if (token) {
      config.headers.set("Authorization", `Bearer ${token}`);
    }

    // FormData 여부 판단
    const isFormData = config.data instanceof FormData;

    if (!isFormData) {
      if (!config.headers.has("Content-Type")) {
        config.headers.set("Content-Type", "application/json");
      }
    }

    return config;
  },
  (error) => Promise.reject(error)
);

// 응답 인터셉터
apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    // async 함수로 변경
    const originalRequest = error.config;

    // 401 에러(Unauthorized)이고, 아직 재시도하지 않은 요청인 경우
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true; // 무한 루프 방지 플래그 설정

      try {
        // apiClient가 아닌 axios 원본을 사용하여 인터셉터 순환 참조 방지
        const response = await axios.post(
          `${API_BASE_URL}/auth/reissue`,
          {},
          { withCredentials: true } // 쿠키 전송 허용
        );

        // 새로운 Access Token 저장
        const newAccessToken = response.data.data?.accessToken;
        if (newAccessToken) {
          localStorage.setItem("accessToken", newAccessToken);

          // 실패했던 원래 요청의 헤더를 새 토큰으로 교체
          originalRequest.headers.set("Authorization", `Bearer ${newAccessToken}`);

          // 원래 요청 다시 수행
          return apiClient(originalRequest);
        }
      } catch (reissueError) {
        // 재발급 실패 시 (Refresh Token 만료 등) -> 로그아웃 처리
        console.error("토큰 재발급 실패:", reissueError);
        localStorage.removeItem("accessToken");
        localStorage.removeItem("user");
        window.location.href = "/login";
        return Promise.reject(reissueError);
      }
    }

    // 401 에러인데 재시도했거나, 재발급 로직이 아닌 일반적인 401의 경우 로그아웃 처리
    if (error.response?.status === 401) {
      localStorage.removeItem("accessToken");
      localStorage.removeItem("user");
      window.location.href = "/login";
    }

    return Promise.reject(error);
  }
);

export default apiClient;

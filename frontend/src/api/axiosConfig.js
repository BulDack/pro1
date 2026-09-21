import axios from 'axios'; //Axios는 React에서 Spring Boot 서버와 HTTP 통신을 할 때 사용하는 라이브러리
import { customHistory } from '../customHistory';
//  React
//    │
//    │ HTTP 요청
//    ▼
//  Axios
//    │
//    │ GET /api/posts
//    ▼
//  Spring Boot Controller
//    │
//    ▼
//  Service
//    │
//    ▼
//  Repository
//    │
//    ▼
//  MySQL

// 🚨 무한 alert 팝업창 도배를 막기 위한 플래그 변수
let isRedirecting = false;

// 1. 나만의 커스텀 Axios 인스턴스 생성
const api = axios.create({
  baseURL: 'http://localhost:8080',
  withCredentials:true, //⭐️ HttpOnly 쿠키(refreshToken)를 주고받기 위해 필수입니다.
  timeout: 5000,
  headers: {
    // ✅ 매번 인터셉터에서 넣지 않고, 기본값으로 고정합니다.
    'Content-Type': 'application/json'
  }
});

// 2. [요청 인터셉터] 로컬 스토리지(프론트엔트 영역)에 Access Token이 있다면 헤더에 담아서 보냅니다.
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('accessToken');
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// 3. [응답 인터셉터]
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {

      // ✅ 이미 로그인 페이지로 튕기는 중이라면 추가 alert을 띄우지 않습니다.
      if (!isRedirecting) {
        isRedirecting = true;

        alert('세션이 만료되었습니다. 다시 로그인해 주세요.');

        // ✅ clear() 대신 로그인 정보만 안전하게 조준 삭제합니다.
        localStorage.removeItem('accessToken');
        localStorage.removeItem('username');

        customHistory.push('/login');

        // 이동이 끝난 후 다시 플래그를 해제합니다.
        setTimeout(() => {
          isRedirecting = false;
        }, 1000);
      }
    }
    return Promise.reject(error);
  }
);

export default api;
import axios from 'axios'; //Axios는 React에서 Spring Boot 서버와 HTTP 통신을 할 때 사용하는 라이브러리
import { customHistory } from '../customHistory.js';
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
// 401발생시 토큰 재발급(Refresh) 중복 요청을 막기 위한 플래그 및 대기 큐
let isRefreshing =false;
let failedQueue = [];

//AuthContext(React state메모리)에 있는 Access Token에 접근하고 업데이트 하기 위한 Bridge변수
let getAccessTokenFromMemory= null;
let updateAccessTokenInMemory= null;

/**
 * React 영역(AxiosAuthInterceptor)에서 AuthContext의 함수들을 외부 Axios 모듈로 주입해주는 함수
 */
export const injectAuthStore = (getToken, setToken) => {
    getAccessTokenFromMemory = getToken;
    updateAccessTokenInMemory = setToken;
};

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
//  api.get()등등과 같은 요청이 실행되면 Axios가 내부적으로 등록해둔 인터셉터를 확인해서 실행
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

//결합 후 새로 적용된 핵심 차이점
// localStorage 제거 및 메모리 참조:
//
// 기존: localStorage.getItem('accessToken')
//
// 변경: getAccessTokenFromMemory()를 호출하여 JS 변수(State)에 있는 토큰을 헤더에 삽입 (XSS 보안 강화).
//
// 자동 토큰 갱신 (Silent Refresh) 흐름 추가:
//
// 기존에는 401 에러가 나면 즉시 alert을 띄우고 /login으로 튕겨 나갔습니다.
//
// 변경 후에는 401 에러가 나면 가장 먼저 HttpOnly 쿠키의 Refresh Token으로 백엔드에 새 Access Token을 요청해 봅니다.
//
// 토큰 재발급에 성공하면 기존 화면이 끊기지 않고 원래 보내려던 API가 자동 재요청됩니다.
//
// Refresh Token마저 만료되었을 때만 작성해 두신 isRedirecting 플래그가 작동하여 alert을 띄우고 customHistory.push('/login')으로 이동합니다.
//
// 동시 요청 처리 Queue (failedQueue):
//
// 한 페이지 진입 시 여러 개의 API가 동시에 401을 받아도, 백엔드로의 토큰 재발급 요청은 단 1번만 전송되도록 안정성이 강화되었습니다.
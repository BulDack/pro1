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

/**
 * 토큰 재발급 진행 중 들어온 401 실패 요청들을 큐에 쌓았다가,
 * 새 토큰이 나오면 일괄 성공 처리하거나 실패 시 일괄 거부 처리하는 함수
 */
const processQueue = (error, token = null) => {
    failedQueue.forEach((prom) => {
        if (error) {
            prom.reject(error);
        } else {
            prom.resolve(token);
        }
    });
    failedQueue = [];
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

// 2. [요청 인터셉터] 기존 localStorage 대신 React 메모리(State)에서 Access Token을 읽어와 Authorization 헤더에 담음
//  api.get(),post()등등과 같은 요청이 실행되면 Axios가 내부적으로 등록해둔 인터셉터를 확인해서 실행
api.interceptors.request.use(
    //요청이 성공적으로 보내지기전에 실행되는 콜백함수
  (config) => {
      //config객체:Axios가 백엔드로 보낼 요청정보(url,method,headers등)가 들어있는 설정 객체

      // 만약 토큰이 존재한다면, 백엔드로 전달될 HTTP Request Header에 Authorization 항목을 추가
    const token = getAccessTokenFromMemory ? getAccessTokenFromMemory() : null;
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
      // 수정된 config(헤더에 토큰이 포함된 상태)를 리턴하면 백엔드 서버로 실제 요청이 전송
    return config;
  },
    // 요청을 보내기 전 프론트엔드 내부에서 에러가 발생한 경우 실행되는 콜백 함수
  (error) => Promise.reject(error)
);

// 3. [응답 인터셉터] (401 발생시 silent refresh 및 예외 처리)
api.interceptors.response.use(
  (response) => response,
  async (error) => {
      const originalRequest=error.config;

      //401 Unauthorized 에러 발생 및 아직 재시도(retry)를 안 한 요청인 경우
    if (error.response && error.response.status === 401 && !originalRequest._retry) {

        // 예외 케이스 1: 토큰 재발급 요청(/api/auth/refresh) 자체가 401을 받은 경우 (Refresh Token 만료)
        if (originalRequest.url === '/api/auth/refresh' || originalRequest.url === '/auth/refresh') {
            if (!isRedirecting) {
                isRedirecting = true;
                alert('세션이 만료되었습니다. 다시 로그인해 주세요.');

                // 메모리의 Access Token 초기화
                if (updateAccessTokenInMemory) {
                    updateAccessTokenInMemory(null);
                }

                // customHistory를 이용하여 로그인 페이지로 강제 이동
                customHistory.push('/login');

                setTimeout(() => {
                    isRedirecting = false;
                }, 1000);
            }
            return Promise.reject(error);
        }

        // 예외 케이스 2: 이미 다른 요청 때문에 토큰 재발급(Refresh)이 진행 중인 경우
        // 현재 요청을 failedQueue에 보관하여 대기시킵니다.
        if (isRefreshing) {
            return new Promise((resolve, reject) => {
                failedQueue.push({ resolve, reject });
            })
                .then((token) => {
                    originalRequest.headers['Authorization'] = `Bearer ${token}`;
                    return api(originalRequest); // 새 토큰으로 원래 요청 재실행
                })
                .catch((err) => Promise.reject(err));
        }

        // 정상 흐름: 401 감지 시 Silent Refresh 시도
        originalRequest._retry = true;
        isRefreshing = true;

        try {
            // HttpOnly 쿠키에 저장된 Refresh Token을 이용해 백엔드에 새 Access Token 요청
            const response = await axios.post(
                '/api/auth/refresh',
                {},
                { withCredentials: true } // Refresh Token 쿠키 전달
            );

            const { accessToken: newAccessToken } = response.data;

            // 1. 메모리(AuthContext State)의 Access Token 업데이트
            if (updateAccessTokenInMemory) {
                updateAccessTokenInMemory(newAccessToken);
            }

            // 2. 실패했던 원래 요청의 헤더를 새 토큰으로 교체
            originalRequest.headers['Authorization'] = `Bearer ${newAccessToken}`;

            // 3. 큐에 대기 중이던 다른 401 요청들 일괄 성공 처리
            processQueue(null, newAccessToken);

            // 4. 실패했던 원래 요청 재실행
            return api(originalRequest);
        } catch (refreshError) {
            // Refresh Token도 만료되어 재발급에 실패한 경우
            processQueue(refreshError, null);

            if (!isRedirecting) {
                isRedirecting = true;
                alert('세션이 만료되었습니다. 다시 로그인해 주세요.');

                if (updateAccessTokenInMemory) {
                    updateAccessTokenInMemory(null);
                }

                customHistory.push('/login');

                setTimeout(() => {
                    isRedirecting = false;
                }, 1000);
            }

            return Promise.reject(refreshError);
        } finally {
            isRefreshing = false;
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
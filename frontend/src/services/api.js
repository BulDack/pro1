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

/*
 * Refresh가 진행 중이면 해당 Promise를 저장한다.
 *
 * null:
 *   현재 진행 중인 Refresh 요청이 없음
 *
 * Promise:
 *   Refresh 요청이 진행 중이며 결과를 기다리는 상태
 */
let refreshPromise = null;



//AuthContext(React state메모리)에 있는 Access Token에 접근하고 업데이트 하기 위한 Bridge변수
let getAccessTokenFromMemory= null;
let updateAccessTokenInMemory= null;

/**
 * React 영역(AxiosAuthInterceptor)에서 AuthContext의 함수들을 외부 Axios 모듈로 주입해주는 함수
 * getToken:AuthContext에 저장된 최신 Access Token반환 함수
 * setToken:AuthContext의 Access Token상태 변경함수
 */
export const injectAuthStore = (getToken, setToken) => {
    getAccessTokenFromMemory = getToken;
    updateAccessTokenInMemory = setToken;
};

/*
 * ============================================================
 * 5. 공통 Silent Refresh 함수
 * ============================================================
 *
 * 이 함수는 두 곳에서 사용된다.
 *
 * ① AuthContext:
 *    페이지 새로고침 후 로그인 상태 복원
 *
 * ② api 응답 인터셉터:
 *    일반 API에서 401이 발생했을 때 토큰 재발급
 *
 * 같은 시간에 여러 곳에서 호출해도
 * 진행 중인 Refresh Promise를 공유한다.
 */
export const refreshAccessToken = () => {

    /*
     * 이미 Refresh 요청이 진행 중이라면
     * 새로운 HTTP 요청을 만들지 않는다.
     *
     * 기존 Promise를 그대로 반환해
     * 동일한 재발급 결과를 기다리게 한다.
     *
     * 예:
     *
     * 게시글 API 401 → Refresh 시작
     * 상품 API 401   → 기존 Promise 공유
     * 회원 API 401   → 기존 Promise 공유
     */
    if (refreshPromise) {
        return refreshPromise;
    }

    /*
     * Refresh 요청이 없다면 새로운 요청 생성
     *
     * authApi는 일반 API의 401 인터셉터가 없으므로
     * 재발급 요청이 실패해도 재귀적으로 재발급하지 않는다.
     *
     */
    refreshPromise = (async () => { //자바 스크립트에서 async 키워드가 붙은 함수는 호출되는 순간
        // 무조건 promise객체를 반환함(내부코드의 성공/실패여부와 상관없이 대기 상테의 promise객체가 생성되서 변수에 할당됨)


        //Axios의 모든http메서드(get,post등등)는 내부적으로 promise를 반환하도록 되어잇음
        //await는 그 promise가 끝날때 까지 기다리는 역할
        const response = await authApi.post('/api/auth/refresh');

        /*
         * authApi에는 응답 변환 인터셉터를 등록하지 않았다.
         *
         * 따라서 실제 응답 구조:
         *
         * response
         *   └─ data                  Axios 응답 Body
         *       ├─ success
         *       ├─ message
         *       └─ data              Spring ApiResponse의 data
         *           └─ accessToken
         *
         * 즉 실제 토큰은 response.data.data.accessToken이다.
         */
        const newAccessToken = response.data?.data?.accessToken;

        /*
         * 백엔드가 200 OK를 반환했더라도
         * 토큰이 없는 잘못된 응답이라면 실패 처리한다.
         */
        if (!newAccessToken) {
            throw new Error(
                'Refresh 응답에 Access Token이 없습니다.'
            );
        }

        /*
         * React AuthContext 메모리에 새 토큰 저장
         *
         * injectAuthStore가 아직 실행되지 않은 경우에는
         * 상태 업데이트 함수를 호출할 수 없으므로 건너뛴다.
         */
        if (updateAccessTokenInMemory) {
            updateAccessTokenInMemory(newAccessToken);
        }

        /*
         * 이 함수의 호출자에게도 새 토큰을 반환한다.
         *
         * Axios 인터셉터는 반환된 토큰을 이용해
         * 실패한 HTTP 요청을 재실행한다.
         */
        return newAccessToken;

    })();

    /*
     * Refresh가 성공하든 실패하든
     * 작업이 끝나면 저장된 Promise를 정리한다.
     *
     * finally()가 반환하는 Promise에 catch()를 연결해
     * 정리용 Promise의 미처리 거부도 방지한다.
     *
     * refreshPromise 자체에는 원래 요청 Promise를 보관한다.
     */
    const currentPromise = refreshPromise;

    void currentPromise.finally(() => {
        if (refreshPromise === currentPromise) {
            refreshPromise = null;
        }
    }).catch(() => {});

    return currentPromise;
};


/*
 * ============================================================
 * 1. Axios 공통 설정
 * ============================================================
 *
 * 두 종류의 Axios 인스턴스에서 공통으로 사용할 설정.
 *
 * api:
 *   게시글, 상품, 회원 정보 등 일반 API 통신 담당
 *
 * authApi:
 *   로그인, 회원가입, Refresh 등 인증 관련 통신 담당
 *
 * 둘을 구분하는 가장 큰 이유는
 * 인증 API에 401 자동 재발급 인터셉터가 적용되는 것을
 * 방지하기 위해서
 */
const baseConfig={
    //spring boot서버 주소
    baseURL: 'http://localhost:8080',

    /*
     * 쿠키 포함 요청을 허용한다.
     *
     * Refresh Token은 HttpOnly 쿠키에 저장되어 있으므로
     * 브라우저가 해당 쿠키를 자동 전송할 수 있어야 한다.
     *
     * 단, 실제 쿠키 전송 여부는
     * Domain, Path, SameSite, Secure, CORS 설정에 따라 결정된다.
     */
    withCredentials: true, //⭐️ HttpOnly 쿠키(refreshToken)를 주고받기 위해 필수

    // 5초 이상 요청이 완료되지 않으면 timeout 발생
    timeout: 5000,

    // headers: { // ✅ 매번 인터셉터에서 넣지 않고, 기본값으로 고정합니다.
    //     'Content-Type': 'application/json'
    // }
}

/*
 * ============================================================
 * 2. Axios 인스턴스 두 개 생성
 * ============================================================
 */

/*
 * 인증 전용 인스턴스
 *
 * 일반 API의 JWT 자동 첨부 인터셉터 및
 * 401 자동 재발급 인터셉터를 등록하지 않는다.
 *
 * 따라서 Refresh 요청 자체가 실패하더라도
 * 다시 Refresh를 호출하는 순환 구조가 발생하지 않는다.
 */
export const authApi = axios.create(baseConfig);

/*
 * 일반 API용 인스턴스
 *
 * JWT Access Token을 Authorization 헤더에 첨부하고,
 * 401 오류가 발생하면 Silent Refresh를 시도한다.
 */
const api = axios.create(baseConfig);


// 2. [요청 인터셉터] 기존 localStorage 대신 React 메모리(State)에서 Access Token을 읽어와 Authorization 헤더에 담음
//  api.get(),post()등등과 같은 요청이 실행되면 Axios가 내부적으로 등록해둔 인터셉터를 확인해서 실행
api.interceptors.request.use(
    //요청이 성공적으로 보내지기전에 실행되는 콜백함수
  (config) => {//config객체:Axios가 백엔드로 보낼 요청정보(url,method,headers등)가 들어있는 설정 객체

      /*
         * 호출자가 AccessToken토큰을 직접 지정했다면 그대로 사용한다.(authContext에서)
         *
         * 예:
         * getMyInfo(방금_발급받은_토큰)
         * React 메모리에 이전 토큰이 남아 있어도
         * 직접 지정한 새 토큰을 덮어쓰지 않는다.
         */
      if(config.headers.has('Authorization')){
          return config;
      }

        /*
         * 직접 지정한 인증 헤더가 없는 일반 요청은
         * 기존 방식대로 메모리의 토큰을 사용한다.
         */
      // 만약 토큰이 존재한다면, 백엔드로 전달될 HTTP Request Header에 Authorization 항목을 추가
    const token = getAccessTokenFromMemory ? getAccessTokenFromMemory() : null;
      // 토큰이 없으면 인증 헤더를 제거한다.
      // * 이전 요청 설정을 재사용할 때 헤더가 남는 것도 방지한다.
      // * 공개 조회 API는 토큰이 없어도 요청할 수 있다.
    if (token) {
        config.headers['Authorization'] = `Bearer ${token}`;
    }else{
        config.headers.delete('Authorization');
    }
      // Axios는 수정된 config(헤더에 토큰이 포함된 상태)를 사용해 리턴하면 백엔드 서버로 실제 요청이 전송
    return config;
  },
    // 요청을 보내기 전 프론트엔드 내부에서 에러가 발생한 경우 실행되는 콜백 함수
  (error) => Promise.reject(error)
);

// 3. [응답 인터셉터] (401 발생시 silent refresh 및 예외 처리)
api.interceptors.response.use(
  (response) => response.data,
  async (error) => {
      /*
       * 실패했던 HTTP 요청 설정을 가져온다.
       *
       * 여기에는 URL, method, headers, data 등이
       * 포함되어 있으므로 재시도할 때 재사용할 수 있다.
       */
      const originalRequest = error.config;
      /*
       * 서버의 HTTP 상태 코드 추출
       * 네트워크 단절 등으로 서버 응답 자체가 없는 경우
       * status는 undefined가 된다.
       */
      const status = error.response?.status;
      /*
         * 로그인 API의 401은
         * Access Token 만료가 아니라
         * 아이디 또는 비밀번호 오류일 수 있다.
         *
         * 따라서 로그인, 회원가입, Refresh 요청은
         * 자동 재발급 대상으로 처리하지 않는다.
         *
         * 로그아웃도 인증 오류가 발생했다고
         * 다시 세션을 복구할 필요는 없으므로 제외한다.
         */

      const authUrls = [
          '/api/auth/login',
          '/api/auth/join',
          '/api/auth/refresh',
          '/api/auth/logout'
      ];

      const isAuthRequest =
          authUrls.includes(originalRequest?.url);

      /*
         * 다음 경우에는 자동 Refresh를 실행하지 않는다.
         *
         * ① 401 오류가 아닌 경우
         * ② 실패한 요청 설정이 없는 경우
         * ③ 이미 재시도한 요청인 경우
         * ④ 인증 API 자체의 오류인 경우
         */
      if (
          status !== 401 ||
          !originalRequest ||
          originalRequest._retry ||
          isAuthRequest
      ) {
          return Promise.reject(error);
      }

      /*
         * 비로그인 사용자도 공개 API에 접근할 수 있다.
         *
         * Access Token이 없는 상태에서 발생한 401은
         * 자동 재발급을 시도하지 않고 호출자에게 전달한다.
         *
         * 로그인하지 않은 사용자가 보호 API에 접근한 상황은
         * PrivateRoute 또는 해당 페이지에서 처리한다.
         */
      const currentToken = getAccessTokenFromMemory
          ? getAccessTokenFromMemory()
          : null;

      if (!currentToken) {
          return Promise.reject(error);
      }

      /*
        * 같은 요청에서 재발급을 반복하지 않도록 표시한다.
        */
      originalRequest._retry = true;

      try {

          /*
           * 공통 Refresh 함수를 호출한다.
           *
           * 다른 요청에서 이미 Refresh가 진행 중이면
           * 같은 Promise를 공유하므로
           * 재발급 요청이 중복되지 않는다.
           */
          const newAccessToken = await refreshAccessToken();//await로 promise에서 값을 꺼냄

          /*
           * 재발급된 토큰으로 Authorization 헤더 교체
           */
          originalRequest.headers.set(
              'Authorization',
              `Bearer ${newAccessToken}`
          );

          /*
           * 기존 API 요청을 다시 실행한다.
           *
           * 예:
           *
           * GET /api/posts/10 → 401
           * POST /api/auth/refresh → 200
           * GET /api/posts/10 → 200
           */
          return api(originalRequest);

      } catch (refreshError) {

          /*
           * Refresh Token이 만료되었거나
           * 서버에서 재발급을 거부한 상황
           *
           * 이전 코드에서는 여기서 무조건
           * alert()과 로그인 페이지 이동을 수행했다.
           *
           * 하지만 공개 페이지의 정상적인 비로그인 사용자를
           * 세션 만료로 처리할 수 있으므로
           * 이 단계에서는 강제 이동하지 않는다.
           *
           * 인증이 필요한 페이지는 PrivateRoute 등에서
           * 비로그인 상태를 감지해 이동시키도록 한다.
           */
          if (updateAccessTokenInMemory) {
              updateAccessTokenInMemory(null);
          }

          return Promise.reject(refreshError);
      }

  }
);

export default api;

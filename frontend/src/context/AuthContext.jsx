import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import {refreshAccessToken} from '@/services/api';
import {loginApi,logoutApi} from "@/api/authApi.js";

const AuthContext = createContext(null); //전체 공유할 context생성

export const AuthProvider = ({children}) =>{

    // 📌 [역할 1] 전역 인증 상태 관리 (JS 메모리 보관)
    const [accessToken,setAccessToken] = useState(null);
    const [isLoading, setIsLoading] =useState(true);

    // -------------------------------------------------------------
    // 📌 [역할 2] 인증 제어 함수 제공 (API 연동 및 State 갱신)
    // -------------------------------------------------------------
    //로그인 함수
    const login =async (loginId,password) =>{
        const data= await loginApi(loginId,password);
        setAccessToken(data.accessToken); //성공시 메모리에 토큰 저장
        return data;
    };

    //로그아웃 함수
    const logout =async()=>{
        try{
            await logoutApi(); //백엔드 쿠키만료 요청
        }finally {
            setAccessToken(null); //메모리 토큰 삭제
        }
    };

    /*
    * ====================================================
    * 1. 앱 최초 실행(+새로고침) 시 인증 상태 복원
    * ====================================================
    *
    * 페이지 새로고침이 발생하면 React 메모리에 저장된
    * Access Token은 사라진다.
    *
    * 따라서 HttpOnly 쿠키의 Refresh Token으로
    * 새로운 Access Token을 발급받아 복원한다.
    *
    * 함수도 객체이기때문에 react컴포넌트가 다시 렌더링되면 객체도 새롭게 다시 생김
    */
    useEffect(() => {

        let active = true;

        const initializeAuth = async () => {
            try {

                /*
                 * api.js에 정의한 공통 재발급 함수 호출
                 *
                 * 성공하면 새 Access Token을 반환한다.
                 */
                const token = await refreshAccessToken();

                /*
                 * AuthProvider가 아직 유지되는 경우에만
                 * 로그인 상태를 갱신한다.
                 */
                if (active) {
                    setAccessToken(token);
                }

            } catch (error) {

                /*
                 * 비로그인 사용자는 Refresh Token이
                 * 존재하지 않을 수도 있다.
                 *
                 * 따라서 Refresh가 실패하더라도
                 * 경고창을 띄우지 않고 비로그인 상태로 둔다.
                 */
                if (active) {
                    setAccessToken(null);
                }

            } finally {

                /*
                 * 인증 상태 복원이 끝나면 로딩 종료
                 */
                if (active) {
                    setIsLoading(false);
                }
            }
        };

        initializeAuth();

        /*
         * 컴포넌트가 언마운트(컴포넌트가 화면에서 완전히 사라지는 순간=다른페이지로 이동해서 기존 컴포넌트가 화면에서 치워질때)된 뒤 응답이 도착하는 경우 상태 변경을 방지한다.
         * 언마운트 됐을때 useEffect안에서 return해준 정리(cleanUp)함수가 실행됨
         */
        return () => {
            active = false;
        };

    }, []); //빈배열을 주면 컴포넌트가 화면에 처음에 나타나고나서 딱한번 실행

    //==================
    //Context의 작동 방식 (방송국 모델)Context는 이 문제를 "방송국과 수신" 방식으로 해결합니다.

    //Context 생성 (createContext): 데이터를 담아둘 전역 방송국(상자)을 만듭니다.
    //데이터 공급 (Provider): 방송국의 송신기 역할을 하는 컴포넌트(AuthProvider 등)로 자식들을 감쌉니다. 이 Provider 안에 공유할 데이터(예: accessToken, updateToken 등)를 싣습니다.
    //데이터 소비 (useContext): 중간에 있는 수많은 컴포넌트들을 무시하고, 데이터가 필요한 깊은 곳의 컴포넌트가 직접 방송국 주파수를 맞춰 데이터를 쏙 빼씁니다 (useAuth() 훅을 통해서 말입니다).

    // Context를 사용할 때의 주의점 (언제 써야 할까?)
    // Context는 매우 편리하지만 모든 곳에 남발하면 안 되는 이유가 있습니다.
    //전역 상태 공유에 적합: 로그인 유저 정보, 앱 테마(다크모드/라이트모드), 언어 설정 등 애플리케이션 전반에 걸쳐 여러 컴포넌트가 공유해야 하는 데이터에 가장 알맞습니다.
    //리렌더링 이슈: Context 안의 데이터(예: accessToken)가 바뀌면, 그 Context를 구독하고 있는 모든 하위 컴포넌트가 강제로 리렌더링됩니다. 따라서 자주 바뀌지 않거나 특정 컴포넌트에서만 쓰이는 국소적인 데이터라면 일반적인 props나 지역 상태(useState)로 두는 것이 성능상 훨씬 유리합니다.
    // -------------------------------------------------------------
    // 📌 [역할 2] 자식 컴포넌트들에게 데이터와 함수 공유
    // -------------------------------------------------------------
    return (
        <AuthContext.Provider
            value={{
                accessToken,
                isLoading,
                login,
                logout,
                updateToken: setAccessToken,
            }}
        >
            {children}
        </AuthContext.Provider>
    );
};

// 3. 각 컴포넌트에서 손쉽게 가져다 쓰기 위한 커스텀 Hook
export const useAuth = () => useContext(AuthContext);

//======== AuthContext의 핵심 역할 4가지=============
// ① 로그인 상태(Access Token)의 전역 저장소 (State)
// JS 메모리(useState)에 accessToken을 들고 있습니다.
//
// 어느 페이지(Dashboard, Profile 등)에 있든 "현재 사용자가 로그인되어 있는가?"를 즉시 확인할 수 있습니다.
//
// ② 로그인 / 로그아웃 / Silent Refresh 비즈니스 로직 제공 (Actions)
// 컴포넌트가 직접 API를 호출하고 복잡한 처리를 하지 않도록, login(), logout(), refreshAccessToken() 함수를 만들어 외부에 제공합니다.
//
// LoginPage.jsx에서는 단지 const { login } = useAuth();로 불러와서 login(loginId, password)만 실행하면 됩니다.
//
// ③ 앱 접속/새로고침 시 초기화 담당 (Silent Refresh 실행)
// AuthContext 내부의 useEffect는 앱이 처음 켜질 때 백엔드로 HttpOnly 쿠키(Refresh Token)를 보내 새로운 Access Token을 받아오는 작업을 자동으로 수행합니다.
//
// 토큰을 받아오는 동안에는 isLoading: true 상태로 만들어 화면이 이상하게 깜빡이거나 비로그인 상태로 잘못 처리되는 것을 막아줍니다.
//
// ④ 비-React 영역(Axios)과의 브릿지(Bridge) 역할
// AxiosAuthInterceptor 컴포넌트를 통해 AuthContext 내의 Access Token 접근자 함수를 api.js로 전달해 줍니다.
//
// 덕분에 Axios 요청 인터셉터가 React Hook을 사용하지 않고도 메모리의 최신 Access Token을 읽어와 요청 헤더에 자동으로 실어 보낼 수 있게 됩니다.
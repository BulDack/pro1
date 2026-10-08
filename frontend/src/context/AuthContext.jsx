import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import axios from 'axios';
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

    // Silent Refresh (페이지 새로고침 시 HttpOnly 쿠키 기반 Access Token 재발급)
    const refreshAccessToken = useCallback(async () => {
        try {
            // HttpOnly 쿠키 기반으로 백엔드에 새 Access Token 요청
            const response = await axios.post('http://localhost:8080/api/auth/refresh', {}, { withCredentials: true });
            setAccessToken(response.data.accessToken);
        } catch (error) {
            setAccessToken(null);
        } finally {
            setIsLoading(false);
        }
    }, []);//useCallback은 함수를 기억햇다가 재사용

    // -------------------------------------------------------------
    // 📌 [역할 3] 앱 실행 시 최우선 자동 실행
    // -------------------------------------------------------------
    useEffect(()=>{
        refreshAccessToken();
    },[refreshAccessToken]); //함수도 객체이기때문에 react컴포넌트가 다시 렌더링되면 객체도 새롭게 다시 생김
                                //useEffect는 두번째 인수값이 바뀌면 다시실행

    // -------------------------------------------------------------
    // 📌 [역할 4] 자식 컴포넌트들에게 데이터와 함수 공유
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
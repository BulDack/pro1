import React from 'react';
import { Navigate, Outlet, useLocation } from 'react-router-dom';
import {useAuth} from '../context/AuthContext.jsx';

/**
 * [ProtectedRoute 기능 및 내부 원리]
 * 1. localStorage 또는 AuthContext/Redux 등에서 JWT 토큰 보유 여부 검증
 * 2. 토큰이 존재하면 <Outlet />을 통해 하위 자식 라우트(컴포넌트)를 렌더링
 * 3. 토큰이 없으면 <Navigate />를 통해 /login으로 리다이렉트
 * 4. state={{ from: location }}을 전달하여 로그인 완료 후 이전 접근 시도 페이지로 복귀 가능
 * =====
 * 프론트엔드에서 페이지접근을 제어하는 기능을 담당하지만 실제 개인정보나 주문데이터 보호는 반드시 SpringSecurity애서도
 * 인증및 권한을 검사해야함
 */

const ProtectedRoute =()=> {
    const {accessToken,isLoading} =useAuth();
    const location= useLocation(); //사용자가 원래 접근하려던 url을 기억

    /*
     * 앱 시작 직후 Silent Refresh가 진행 중이라면
     * 아직 로그인 여부를 확정할 수 없다.
     *
     * 따라서 로그인 페이지로 이동시키지 않고
     * 인증 확인이 끝날 때까지 로딩 화면을 표시한다.
     */
    //앱 최초 접속시 Silent Refresh 완료전까지 로딩 표시
    if(isLoading){
        return <div style={{ padding: '2rem', textAlign: 'center' }}>인증 상태 확인 중...</div>;
    }

    /*
     * 인증 확인이 끝났는데 Access Token이 없다면
     * 로그인 페이지로 이동시킨다.
     *
     * replace:
     * 현재 히스토리 항목을 로그인 페이지로 교체한다.
     *
     * state:
     * 로그인 이후 원래 요청한 페이지로 돌아가기 위해
     * 기존 위치 정보를 전달한다.
     */
    //메모리에 Access Token이 없으면 로그인 페이지로 리다이렉트
    if (!accessToken){
        return <Navigate to="/login" state={{ from: location }} replace />;
    }

    //로그인된(AccessToken이 있는)사용자라면 자식 라우트를 렌더링 함.
    // /mypage → MyPage , /orders → OrderPage
    return <Outlet />;
};

export default ProtectedRoute;
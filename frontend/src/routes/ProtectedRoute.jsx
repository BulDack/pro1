import React from 'react';
import { Navigate, Outlet, useLocation } from 'react-router-dom';
import {useAuth} from '../context/AuthContext.jsx';

/**
 * [ProtectedRoute 기능 및 내부 원리]
 * 1. localStorage 또는 AuthContext/Redux 등에서 JWT 토큰 보유 여부 검증
 * 2. 토큰이 존재하면 <Outlet />을 통해 하위 자식 라우트(컴포넌트)를 렌더링
 * 3. 토큰이 없으면 <Navigate />를 통해 /login으로 리다이렉트
 * 4. state={{ from: location }}을 전달하여 로그인 완료 후 이전 접근 시도 페이지로 복귀 가능
 */

const ProtectedRoute =()=> {
    const {accessToken,isLoading} =useAuth();
    const location= useLocation();

    //앱 최초 접속시 Silent Refresh 완료전까지 로딩 표시
    if(isLoading){
        return <div style={{ padding: '2rem', textAlign: 'center' }}>인증 상태 확인 중...</div>;
    }

    //메모리에 Access Token이 없으면 로그인 페이지로 리다이렉트
    if (!accessToken){
        return <Navigate to="/login" state={{ from: location }} replace />;
    }
    return <Outlet />;
};

export default ProtectedRoute;
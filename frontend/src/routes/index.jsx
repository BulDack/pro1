//모든 라우트와 레이아웃(mainLayout,AuthLayout.jsx)과 인증가드(ProtectedRoute)를
//하나의 전체 라우터 트리로 결합하여 createBrowserRouter 객체로 생성해 저장
import React, { Suspense, lazy } from 'react';
import { createBrowserRouter } from 'react-router-dom';
import MainLayout from '../layouts/MainLayout';
import AuthLayout from '../layouts/AuthLayout.jsx';
import ProtectedRoute from './ProtectedRoute';
import { publicRoutes } from './publicRoutes';
import { privateRoutes } from './privateRoutes';


const LoginPage = lazy(() => import('../pages/auth/LoginPage'));
const Join = lazy(() => import('../pages/auth/Join'));

export const router = createBrowserRouter([
    // 1. 공통 레이아웃 (Header, Footer 포함) 라우트
    {
        path: '/',
        element: (
            <Suspense fallback={<div>Loading...</div>}>
                <MainLayout />
            </Suspense>
        ),
        children: [
            ...publicRoutes,
            {
                element: <ProtectedRoute />, // 인증된 사용자만 접근 가능,자식라우트들에 공통으로 인증검사를 적용하는 용도
                children: privateRoutes,
            },
        ],
    },
    // 2. 인증 레이아웃 (로그인 / 회원가입 전용 독립 레이아웃) 라우트
    {
        element: (
            <Suspense fallback={<div>로딩 중... (스피너 표시)</div>}>
                <AuthLayout />
            </Suspense>
        ),
        children: [
            {
                path: 'login',
                element: <LoginPage />,
            },
            {
                path: 'join',
                element: <Join />,
            }
        ],
    },
]);
//lazy의 역할:시작할때 모두 불러오는게 아니라 필요할때 지연로딩!
//Suspense의 역할: lazy로 불러온 컴포넌트가 아직 다운로드 중일 때,
//사용자가 지루하지 않도록 fallback 속성에 지정한 로딩 화면(스피너 등)을 대신 띄워주고,
//다운로드가 끝나면 자연스럽게 진짜 컴포넌트로 바꿔치기 해줍니다.
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
                element: <ProtectedRoute />, // 인증된 사용자만 접근 가능
                children: privateRoutes,
            },
        ],
    },
    // 2. 인증 레이아웃 (로그인 / 회원가입 전용 독립 레이아웃) 라우트
    {
        element: (
            <Suspense fallback={<div>Loading...</div>}>
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
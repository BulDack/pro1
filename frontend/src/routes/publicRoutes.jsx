import React,{lazy} from 'react'
//로그인 없이 누구나 접근할 수 있는 경로 목록을 배열객체 형태롤 저장

const HomePage =lazy(()=>import('../pages/home/HomePage'));
const LoginPage =lazy(()=>import('../pages/auth/LoginPage'));
const Join =lazy(()=>import('../pages/auth/Join'));

const NotFoundPage =lazy(()=>import('../pages/NotFoundPage.jsx'));

export const publicRoutes= [
    {index: true,element: <HomePage /> },
    {index: 'login' ,element: <LoginPage /> },
    {index: 'join' , element: <Join /> },
    {path: '*',element:<NotFoundPage /> },
];
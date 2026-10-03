import React,{lazy} from 'react'

const HomePage =lazy(()=>import('../pages/HomePage'));
const LoginPage =lazy(()=>import('../pages/LoginPage'));
const NotFoundPage =lazy(()=>import('../pages/NotFoundPages'));

export const publicRoutes= [
    {index: true,element: <HomePage /> },
    {index: 'login' ,element: <LoginPage /> },
    {path: '*',element:<NotFoundPage /> },
];
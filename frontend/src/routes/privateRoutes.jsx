//jwt인증이 반드시 필요한 라우트 목록 객체
import React,{lazy} from 'react';

const Mypage= lazy(()=>import('../pages/member/Mypage'));

export const privateRoutes=[
    {
        path: 'mypage',
        element: <Mypage />,
    },
];
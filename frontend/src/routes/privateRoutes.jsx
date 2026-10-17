//jwt인증이 반드시 필요한 라우트 목록 객체
import React,{lazy} from 'react';

const Mypage= lazy(()=>import('../pages/member/Mypage'));
const PostCreate=lazy(()=>import('../pages/post/PostCreate.jsx'));
const PostUpdate=lazy(()=>import('../pages/post/PostUpdate.jsx'));


export const privateRoutes=[
    {path: 'my-page', element: <Mypage />},
    {path: 'posts/new', element: <PostCreate />},
    {path: 'posts/:postId/edit', element: <PostUpdate />},

];
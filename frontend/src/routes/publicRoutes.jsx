/* eslint-disable react-refresh/only-export-components -- 이 파일은 화면 대신 라우트 설정 배열을 내보냅니다. */
import {lazy} from 'react'
//로그인 없이 누구나 접근할 수 있는 경로 목록을 배열객체 형태롤 저장

const HomePage =lazy(()=>import('../pages/home/HomePage'));
const MainPage =lazy(()=>import('../pages/home/MainPage'));
const PostDetail=lazy(()=>import('../pages/post/PostDetail.jsx'));
const PostList=lazy(()=>import('../pages/post/PostList.jsx'));
const ItemList=lazy(()=>import('../pages/item/itemListPage.jsx'));
const ItemDetail=lazy(()=>import('../pages/item/itemDetailPage.jsx'));




const NotFoundPage =lazy(()=>import('../pages/NotFoundPage.jsx'));

export const publicRoutes= [
    {index: true,element: <HomePage /> },
    {path: 'main', element: <MainPage /> },
    {path: '*',element:<NotFoundPage /> },
    {path: 'posts',element:<PostList /> },
    {path: 'posts/:postId',element:<PostDetail /> },
    {path: 'items/',element:<ItemList /> },
    {path: 'items/:itemId',element:<ItemDetail /> },
];

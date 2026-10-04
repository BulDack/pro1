//공통레이아웃 구성
//페이지 이동 시마다 공통 Header, Navigation, Footer를 매번 선언하지 않고 Outlet을 사용하여 라우팅과 레이아웃을 분리
import React from 'react';
import { Outlet } from 'react-router-dom';
import Header from '../components/Header';

const MainLayout = ()=> {
    return(
        <div className= "main-layout">
            <Header />
            <main style={{padding: '2rem'}}>
                {/* 현재 URL 라우트 컴포넌트가 위치하는 영역 */}
                <Outlet />
            </main>
            <footer style={{ textAlign: 'center', padding: '1rem', borderTop: '1px solid #ccc' }}>
                © 2026 My 1st App. All rights reserved.
            </footer>
        </div>
    );
};

export default MainLayout;
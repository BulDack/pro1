import React from 'react';
import { Outlet } from 'react-router-dom';

const AuthLayout = () =>{
    return(
        <div className="auth-layout" style={{ minHeight: '100vh', display: 'flex', justifyContent: 'center', alignItems: 'center', background: '#fafafa' }}>
            <div style={{ width: '100%', maxWidth: '400px', padding: '2rem', background: '#fff', boxShadow: '0 2px 8px rgba(0,0,0,0.1)' }}>
                {/* 로그인 / 회원가입 페이지가 위치하는 영역 */}
                <Outlet />
            </div>
        </div>
    );
};

export default AuthLayout;
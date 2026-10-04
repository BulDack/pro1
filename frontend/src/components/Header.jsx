import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const Header = () => {
    const { accessToken, logout } = useAuth();
    const navigate = useNavigate();

    const handleLogout = async () => {
        await logout();
        navigate('/login');
    };

    return (
        <header style={{ display: 'flex', justifyContent: 'space-between', padding: '1rem', background: '#f5f5f5' }}>
            <div className="logo">
                <Link to="/">My Application</Link>
            </div>
            <nav>
                <Link to="/" style={{ marginRight: '1rem' }}>홈</Link>
                {accessToken ? (
                    <>
                        <Link to="/mypage" style={{ marginRight: '1rem' }}>마이페이지</Link>
                        <button onClick={handleLogout}>로그아웃</button>
                    </>
                ) : (
                    <Link to="/login">로그인</Link>
                )}
            </nav>
        </header>
    );
};

export default Header;
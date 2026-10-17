
import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './Header.css';

const Header = () => {
    const { accessToken, logout } = useAuth();
    const navigate = useNavigate();

    const handleLogout = async () => {
        await logout();
        navigate('/login');
    };

    return (
        <header className="header">
            <div className="header-container">

                <div className="logo">
                    <Link to="/">
                        Kirby World!
                    </Link>
                </div>

                <nav className="nav-menu">

                    <Link to="/" className="nav-link">
                        홈
                    </Link>

                    {accessToken ? (
                        <>
                            <Link
                                to="/my-page"
                                className="nav-link"
                            >
                                마이페이지
                            </Link>

                            <button
                                className="logout-button"
                                onClick={handleLogout}
                            >
                                로그아웃
                            </button>
                        </>
                    ) : (
                        <Link
                            to="/login"
                            className="login-button"
                        >
                            로그인
                        </Link>
                    )}

                </nav>
            </div>
        </header>
    );
};

export default Header;

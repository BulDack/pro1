// import React from 'react';
// import { Link, useNavigate } from 'react-router-dom';
// import { useAuth } from '../context/AuthContext';
//
// const Header = () => {
//     const { accessToken, logout } = useAuth();
//     const navigate = useNavigate();
//
//     const handleLogout = async () => {
//         await logout();
//         navigate('/login');
//     };
//
//     return (
//         <header style={{ display: 'flex', justifyContent: 'space-between', padding: '1rem', background: '#f5f5f5' }}>
//             <div className="logo">
//                 <Link to="/">메인 페이지</Link>
//             </div>
//             <nav>
//                 <Link to="/" style={{ marginRight: '1rem' }}>홈</Link>
//                 {accessToken ? (
//                     <>
//                         <Link to="/my-page" style={{ marginRight: '1rem' }}>마이페이지</Link>
//                         <button onClick={handleLogout}>로그아웃</button>
//                     </>
//                 ) : (
//                     <Link to="/login">로그인</Link>
//                 )}
//             </nav>
//         </header>
//     );
// };
//
// export default Header;
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

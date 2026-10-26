
import React from 'react';
import {Link, useLocation, useNavigate} from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './Header.css';

const Header = () => {
    const { accessToken, logout } = useAuth();
    const navigate = useNavigate();
    const location = useLocation();

    const handleLogout = async () => {


        try{
            /*
         * 서버 로그아웃 API 호출 및
         * AuthContext 인증 상태 초기화.
         */
            await logout();
            /*
       * 현재 페이지가 인증 필수 페이지인지 확인.
       *
       * 실제 보호 경로 목록은 네 라우터 설정과
       * 일치하도록 관리해야 한다.
       */
            const isPrivatePage =
                location.pathname === "/auth/me" ||
                location.pathname.startsWith("/auth/me/");

            if(isPrivatePage){
                //비로그인 상태로 접근할수 없는 페이지
                navigate("/",{replace:true});;
            }
            /*
         * 공개 페이지라면 navigate를 호출하지 않는다.
         *
         * 따라서 사용자는 현재 보고 있던 게시글이나
         * 상품 페이지에 그대로 머무르게 된다.
         */
        }catch(error) {
            console.error("로그아웃 실패",error);
        }

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
                                to="/auth/me"
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
                            state={{from: location }}
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

//      Link와 navigate 차이
// 구분	     Link	                  navigate()
// 사용 방식	JSX 컴포넌트  	      JavaScript 함수
// 주요 목적	사용자가 클릭해서 이동	  특정 로직 실행 후 이동
// 로그인 페이지 이동	추천	            가능
// 로그인 성공 후 이동	부적합	        추천
// state 전달	가능	                가능
// 브라우저 히스토리 관리	가능	        가능

//즉, 사용자가 클릭해서 페이지를 이동할 때는 Link,
//API 처리 결과에 따라 코드에서 이동시킬 때는 navigate()
//header에선 단순히 로그인으로 이동하는거기때문에 link 가적합,
// loginPage.jsx같은 경우는 api를 호출해서 로직에 따라 이동해야 하므로 navigate가 적합
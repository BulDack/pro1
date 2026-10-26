import { Outlet, useMatch } from 'react-router-dom';

export default function AuthLayout() {
    // 회원가입은 Join.jsx가 자체 카드와 반응형 너비를 갖고 있다.
    // 이 경로에서는 부모의 400px 제한을 해제해야 Join.css의 1080px이 적용된다.
    const isJoinPage = Boolean(useMatch('/join'));
    // 로그인은 LoginPage.css에서 카드와 여백을 관리하므로 부모 카드를 중복 표시하지 않는다.
    // 다른 인증 화면은 기존 레이아웃을 사용한다.
    const isLoginPage = Boolean(useMatch('/login'));
    const hasOwnCard = isJoinPage || isLoginPage;

    return (
        <div
            className="auth-layout"
            style={{
                width: '100%',
                minHeight: '100vh',
                display: 'flex',
                justifyContent: 'center',
                alignItems: isJoinPage ? 'flex-start' : 'center',
                background: '#fafafa',
            }}
        >
            <div
                style={{
                    width: '100%',
                    minWidth: 0,
                    // 회원가입 카드의 크기와 여백은 Join.css에서 관리한다.
                    // 부모에도 패딩과 그림자를 주면 카드가 중복되고 입력 영역이 좁아진다.
                    maxWidth: hasOwnCard ? 'none' : '400px',
                    padding: hasOwnCard ? 0 : '2rem',
                    background: hasOwnCard ? 'transparent' : '#fff',
                    boxShadow: hasOwnCard ? 'none' : '0 2px 8px rgba(0,0,0,0.1)',
                }}
            >
                <Outlet />
            </div>
        </div>
    );
}

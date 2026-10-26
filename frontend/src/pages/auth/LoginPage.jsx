import { useState } from 'react';
import './LoginPage.css';
import { useLocation,useNavigate,Link } from 'react-router-dom';
import { useAuth } from '@/context/AuthContext'; // 1. 전역 인증 관리를 위한 useAuth Hook 불러오기

export default function LoginPage() {
  //입력폼 상태 관리
  const [loginId, setLoginId] = useState('');
  const [password, setPassword] = useState('');
  //UI상태 관리(에러 메세지 및 제출 로딩)
  const [errorMessage, setErrorMessage] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const navigate = useNavigate();
  const location =useLocation();
  // AuthContext에서 전역 login 함수 가져오기 (메모리 상태 업데이트 담당)
    const {login} =useAuth();


    /*
      * ProtectedRoute에서 전달한
      * 로그인 이전 페이지 주소를 가져온다.
      *
      * 이전 페이지가 없다면 /main으로 이동한다.
      */
    const from = location.state?.from;
    const redirectPath = from
        ? `${from.pathname}${from.search ?? ""}${from.hash ?? ""}`
        : "/main";



  //===폼 제출 핸들러
  const handleLoginSubmit = async (e) => {
    // ⚠️ 매우 중요: 브라우저가 기본적으로 페이지를 새로고침하며 Form을 전송하는 것을 차단
    e.preventDefault();

    // [프론트엔드 1차 유효성 검사] 공백만 입력된 경우 예방
        if (!loginId.trim() || !password.trim()) {
          setErrorMessage('아이디와 비밀번호를 모두 입력해 주세요.');
          return;
        }

    setIsLoading(true);
    setErrorMessage('');

    try {
      // AuthContext의 login 함수 실행
      // (내부적으로 authApi.js를 호출하고, 수신한 AccessToken을 JS 메모리 State에 저장함)
      await login(loginId,password);

        // 페이지 리로드 없이 리액트 라우터를 이용해 마이페이지/주문목록으로 부드럽게 이동(로그인 성공후 지정된 페이지로 이동)
        navigate(redirectPath,{replace:true});

    } catch (error) {
      console.error('로그인 실패',error);
      // 백엔드 Custom ExceptionHandler가 반환한 에러 메시지가 있다면 매핑
      if (error.response?.data?.message) {
        setErrorMessage(error.response.data.message);
      } else {
        setErrorMessage('서버와의 통신이 원활하지 않습니다.');
      }
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <section className="login-page" aria-labelledby="login-title">
      <div className="login-page__card">
        <header className="login-page__header">
          <Link to="/" className="login-page__brand">KIRBY WORLD</Link>
          <div className="login-page__welcome-icon" aria-hidden="true">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round">
              <rect x="5" y="10" width="14" height="11" rx="3" />
              <path d="M8 10V7a4 4 0 0 1 8 0v3M12 14v3" />
            </svg>
          </div>
          <h1 id="login-title">다시 만나 반가워요</h1>
          <p>로그인하고 새로운 이야기와 상품을 만나보세요.</p>
        </header>

        {/* 기존 제출 함수를 그대로 연결한다. 이 화면은 인증 상태를 별도로 저장하지 않는다.
            오류 알림은 보조기기에 전달하고, 입력란과도 연결해 원인을 확인할 수 있게 한다. */}
        {errorMessage && <p id="login-error" className="login-page__error" role="alert">{errorMessage}</p>}
        <form className="login-page__form" onSubmit={handleLoginSubmit}
              aria-busy={isLoading} aria-describedby={errorMessage ? 'login-error' : undefined}>
          <div className="login-page__field">
            <label htmlFor="login-id">아이디</label>
            <div className="login-page__input-wrap">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" aria-hidden="true">
                <circle cx="12" cy="8" r="3.5" /><path d="M5 21v-2a7 7 0 0 1 14 0v2" />
              </svg>
              <input id="login-id" name="username" type="text" autoComplete="username"
                     value={loginId} onChange={(e) => setLoginId(e.target.value)}
                     placeholder="아이디를 입력해 주세요" required
                     autoCapitalize="none" spellCheck={false}
                     aria-describedby={errorMessage ? 'login-error' : undefined} />
            </div>
          </div>
          <div className="login-page__field">
            <label htmlFor="login-password">비밀번호</label>
            <div className="login-page__input-wrap">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
                <rect x="5" y="10" width="14" height="11" rx="3" /><path d="M8 10V7a4 4 0 0 1 8 0v3" />
              </svg>
              {/* current-password로 기존 비밀번호 자동완성을 지원한다.
                  기존 제어 입력과 required 검증을 유지하고 비밀번호를 별도 저장하지 않는다. */}
              <input id="login-password" name="password" type="password" autoComplete="current-password"
                     value={password} onChange={(e) => setPassword(e.target.value)}
                     placeholder="비밀번호를 입력해 주세요" required
                     aria-describedby={errorMessage ? 'login-error' : undefined} />
            </div>
          </div>
          {/* 요청 중 제출 버튼을 비활성화하는 기존 동작을 유지한다. */}
          <button className="login-page__submit" type="submit" disabled={isLoading}>
            {isLoading && <span className="login-page__spinner" aria-hidden="true" />}
            <span>{isLoading ? '인증 중...' : '로그인'}</span>
            {!isLoading && <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true"><path d="M5 12h14m-6-6 6 6-6 6" /></svg>}
          </button>
          <span className="login-page__sr-only" role="status">{isLoading ? '로그인 정보를 확인하고 있습니다.' : ''}</span>
        </form>

        <div className="login-page__join">
          <p>아직 회원이 아니신가요?</p>
          <Link to="/join">회원가입 <span aria-hidden="true">↗</span></Link>
        </div>
        <Link to="/" className="login-page__home"><span aria-hidden="true">←</span> 홈으로 돌아가기</Link>
      </div>
    </section>
  );
}

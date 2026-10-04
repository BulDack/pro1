import React, { useState } from 'react';
import { useNavigate,Link } from 'react-router-dom';
import { useAuth } from '@/context/AuthContext'; // 1. 전역 인증 관리를 위한 useAuth Hook 불러오기

export default function LoginPage() {
  //입력폼 상태 관리
  const [loginId, setLoginId] = useState('');
  const [password, setPassword] = useState('');
  //UI상태 관리(에러 메세지 및 제출 로딩)
  const [errorMessage, setErrorMessage] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const navigate = useNavigate();

  // AuthContext에서 전역 login 함수 가져오기 (메모리 상태 업데이트 담당)
  const {login} =useAuth();

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
        navigate('/my-page/orders');

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
    <div style={{ maxWidth: '380px', margin: '100px auto', padding: '30px', border: '1px solid #ccc', borderRadius: '8px' }}>
      <h2>마이페이지 로그인</h2>
      {errorMessage && <p style={{ color: 'red', fontSize: '9pt' }}>{errorMessage}</p>}

      <form onSubmit={handleLoginSubmit}>
        <div style={{ marginBottom: '15px' }}>
          <label style={{ display: 'block', marginBottom: '5px' }}>아이디</label>
          <input
            type="text"
            value={loginId}
            onChange={(e) => setLoginId(e.target.value)}
            placeholder="아이디를 입력하세요"
            required
            style={{ width: '100%', padding: '8px', boxSizing: 'border-box' }}
          />
        </div>
        <div style={{ marginBottom: '20px' }}>
          <label style={{ display: 'block', marginBottom: '5px' }}>비밀번호</label>
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            placeholder="비밀번호를 입력하세요"
            required
            style={{ width: '100%', padding: '8px', boxSizing: 'border-box' }}
          />
        </div>
        <button type="submit" disabled={isLoading} style={{ width: '100%', padding: '10px', backgroundColor: '#007bff', color: 'white', border: 'none', cursor: 'pointer' }}>
          {isLoading ? '인증 중...' : '로그인'}
        </button>
      </form>

      <div>
           <Link to="/join">
                회원가입
           </Link>
      </div>
    </div>
  );
}
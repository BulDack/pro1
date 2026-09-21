import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../../api/axiosConfig';

export default function LoginPage() {
  const [loginId, setLoginId] = useState('');
  const [password, setPassword] = useState('');
  const [errorMessage, setErrorMessage] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const navigate = useNavigate();

  const handleLoginSubmit = async (e) => {
    // ⚠️ 매우 중요: 브라우저가 기본적으로 페이지를 새로고침하며 Form을 전송하는 것을 차단
    e.preventDefault();
    setIsLoading(true);
    setErrorMessage('');

    try {
      // 1. JSON 방식으로 백엔드 서버에 로그인 요청을 보냄 (@RequestBody로 수신됨)
      const response = await api.post('/api/auth/login', {
        loginId: loginId,
        password: password
      });

      // 2. 앞서 정한 백엔드 공통 응답 포맷(ApiResponse<T>)을 구조 분해 할당으로 파싱
      const { success, message, data } = response.data;

      if (success && data?.accessToken) {
        // 3. 로그인 성공 시 발급받은 JWT(AccessToken)를 브라우저 로컬 스토리지에 보관
        localStorage.setItem('accessToken', data.accessToken);
        localStorage.setItem('username', data.username);

        // 4. 페이지 리로드 없이 리액트 라우터를 이용해 마이페이지/주문목록으로 부드럽게 이동
        navigate('/my-page/orders');
      } else {
        setErrorMessage(message || '로그인에 실패했습니다.');
      }
    } catch (error) {
      // 백엔드 Custom ExceptionHandler가 반환한 에러 메시지가 있다면 매핑
      if (error.response && error.response.data) {
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
            required
            style={{ width: '100%', padding: '8px', boxSizing: 'border-box' }}
          />
        </div>
        <button type="submit" disabled={isLoading} style={{ width: '100%', padding: '10px', backgroundColor: '#007bff', color: '#white', border: 'none', cursor: 'pointer' }}>
          {isLoading ? '인증 중...' : '로그인'}
        </button>
      </form>
    </div>
  );
}
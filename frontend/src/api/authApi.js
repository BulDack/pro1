// src/api/authApi.js

/**
 * 백엔드 서버로 로그인 요청을 보내는 API 통신 함수
 */
export const loginApi = async (email, password) => {
  try {
    const response = await fetch('https://api.example.com/v1/login', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ email, password }),
    });

    if (!response.ok) {
      const errorData = await response.json();
      throw new Error(errorData.message || '로그인에 실패했습니다.');
    }

    // 로그인 성공 시 서버에서 전달한 토큰(Token) 및 사용자 정보 반환
    const data = await response.json();
    return data;
  } catch (error) {
    console.error('API Error:', error);
    throw error; // 에러를 상위(UI 컴포넌트)로 전파
  }
};
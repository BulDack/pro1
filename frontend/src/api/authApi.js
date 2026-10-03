import api from '../services/api';

/**
 * 백엔드 서버로 로그인 요청을 보내는 API 통신 함수
 */
/**
 * [로그인 API]
 * - api.js에 설정된 baseURL('http://localhost:8080')과 withCredentials: true가 자동 적용됨.
 * - 성공 시 Response Body(accessToken 등)를 반환하고,
 *   HttpOnly Refresh Token 쿠키는 브라우저가 자동으로 수신함.
 */
export const loginApi = async (loginId, password) => {
  try {
    const response = await api.post('/api/auth/login', { loginId, password });
      return response.data; //백엔드에서 넘겨준 json 데이터 반환
    }catch(error){
      // Axios 에러 객체에서 백엔드가 보낸 에러 메시지 추출 및 예외 던지기
      const errorMessage=error.response?.data?.message || '로그인에 실패했습니다.';
      throw new Error(errorMessage);
    }
};

//회원가입 api
export const registerApi=async (userData) =>{
  try{
    const response =await api.post('api/auth/join',userData);
    return response.data;
  }catch (error){
    const errorMessage=error.response?.data?.message ||'회원가입에 실패하였습니다.';
    throw new Error(errorMessage);
  }
};

//로그아웃 api
export const logoutApi = async () => {
  try {
    const response = await api.post('/api/auth/logout');
    return response.data;
  } catch (error) {
    console.error('Logout API Error:', error);
    throw error;
  }
};
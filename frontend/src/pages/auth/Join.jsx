import React, { useState } from 'react';
import {useNavigate,Link} from 'react-router-dom';
import api from '@/services/api.js';

const Join = () => {
  const [formData, setFormData] = useState({
    username: '',
    loginId: '', // JoinRequestDto 필드에 맞게 수정하세요
    password: '',
    passwordConfirm: '', //패스워드 확인값은 굳이 보낼필요 없음
    email: '',
  });

  const [errorMessage,setErrorMessage]=useState('');
  const [isLoading,setIsLoading]=useState(false);

  const navigate=useNavigate();


  //입력값 변경 처리 해주는 함수
  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value
    }));
  };

  const handleJoinSubmit = async (e) => {
    e.preventDefault();

    setErrorMessage('');

    console.log('회원가입 버튼이 클릭되었습니다!', formData);
    //비밀번호 확인
    if((formData.password!=formData.passwordConfirm)){
        setErrorMessage('비밀번호가 일치하지 않습니다.');
        return;
    }

    //  💡 passwordConfirm을 제외한 나머지 필드만 requestData로 추출
    const { passwordConfirm, ...requestData } = formData;

    console.log('실제 백엔드로 전송되는 데이터:', requestData);
    /* 출력 결과:
       {
         username: 'ㅁㅁㅁ',
         loginId: 'aaa',
         password: '111',
         email: 'dsfssdf@naver.com',
         city: '서울',
         street: '중랑구',
         zipcode: '1919'
       }
    */

    setIsLoading(true);

    try {
      // 백엔드 컨트롤러의 join 메서드 호출
      const response = await api.post('/api/auth/join', requestData);
      //여기까지 왔다면 http 200ok이고 무조건 성공(success: true)상태임!
      alert(response.data.message); // "회원가입이 완료되었습니다." 출력 (백엔드단에 있는 membercontroller에서
                            //return ResponseEntity.ok("회원가입이 완료되었습니다.");이렇게 처리 했기 때문에 가능)
      //회원가입 완료후 로그인 페이지로 이동
      navigate('/login');

    } catch (error) {
      //4xx,5xx 에러는 모두 catch로 들어옴
      //이때 서버가 내려준 ApiResponse 객체는 error.response.data에 들어있음
      console.error('회원가입 실패:', error);

      // 1) 백엔드 GlobalExceptionHandler가 ApiResponse.fail로 넘겨준 에러 메시지가 있는 경우
      if (error.response?.data?.message){
       setErrorMessage(error.response.data.message);// 서버가 전달한 에러메시지
        } else {
       setErrorMessage('회원가입에 실패했습니다.'); //네트워크 에러등 기본 메시지
        }
      } finally { setIsLoading(false); }

  };

  return (
      <>
    <form onSubmit={handleJoinSubmit}>
      <h2>회원가입</h2>

      {/* 💡 에러 메시지가 있을 경우 화면에 빨간색으로 표시 */}
      {errorMessage && (
          <p style={{ color: 'red', fontWeight: 'bold' }}>{errorMessage}</p>
      )}

      <div>
        <label>이름</label>
        <input type="text" name="username" placeholder="이름" onChange={handleChange} required />
      </div>

      <div>
        <label>아이디</label>
      <input type="text" name="loginId" placeholder="아이디" onChange={handleChange} required />
      </div>

      <div>
        <label>비밀번호</label>
      <input type="password" name="password" placeholder="비밀번호" onChange={handleChange} required />
      </div>

      <div>
         <label>비밀번호 확인</label>
      <input type="password" name="passwordConfirm" placeholder="비밀번호를 다시 입력하세요" onChange={handleChange} required />
      </div>

      <div>
         <label>이메일</label>
      <input type="email" name="email" placeholder="이메일" onChange={handleChange} required />
      </div>

      <div>
        <label>도시</label>
        <input type="text" name="city" placeholder="도시" onChange={handleChange} required />
      </div>

      <div>
        <label>street</label>
        <input type="text" name="street" placeholder="street" onChange={handleChange} required />
      </div>

      <div>
        <label>우편번호</label>
        <input type="text" name="zipcode" placeholder="우편번호" onChange={handleChange} required />
      </div>

      <button type="submit" disabled={isLoading}>
        {isLoading ? '가입중...' : '회원가입'}
      </button>
    </form>

    <div>
        <Link to="/login">
            로그인으로 돌아가기
        </Link>
    </div>
      </>
  );
};

export default Join;
import React, { useState } from 'react';
import {useNavigate,Link} from 'react-router-dom';
import api from '@/api/axiosConfig';

const Join = () => {
  const [formData, setFormData] = useState({
    loginId: '', // JoinRequestDto 필드에 맞게 수정하세요
    password: '',
    //패스워드 확인값은 굳이 보낼필요 없음
    email: '',
  });

  const [passwordConfirm,setPasswordConfirm]=useState('');
  const [errorMessage,setErrorMessage]=useState('');
  const [isLoading,setIsLoading]=useState(false);

  const navigate=useNavigate();


  //입력값 변경 처리 해주는 함수
  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleJoinSubmit = async (e) => {
    e.preventDefault();

    setErrorMessage('');

    //비밀번호 확인
    if((formData.password!=passwordConfirm)){
        setErrorMessage('비밀번호가 일치하지 않습니다.');
        return;
    }

    setIsLoading(true);

    try {
      // 백엔드 컨트롤러의 join 메서드 호출
      const response = await api.post('/join', formData);
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
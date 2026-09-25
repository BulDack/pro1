import React, { useState } from 'react';
import api from './api';

const Join = () => {
  const [formData, setFormData] = useState({
    username: '', // JoinRequestDto 필드에 맞게 수정하세요
    password: '',
    email: '',
  });

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleJoin = async (e) => {
    e.preventDefault();
    try {
      // 백엔드 컨트롤러의 join 메서드 호출
      const response = await api.post('/join', formData);
      alert(response.data); // "회원가입이 완료되었습니다." 출력
    } catch (error) {
      console.error('회원가입 실패:', error);
      alert('회원가입에 실패했습니다.');
    }
  };

  return (
    <form onSubmit={handleJoin}>
      <h2>회원가입</h2>
      <input type="text" name="username" placeholder="아이디" onChange={handleChange} required />
      <input type="password" name="password" placeholder="비밀번호" onChange={handleChange} required />
      <input type="email" name="email" placeholder="이메일" onChange={handleChange} required />
      <button type="submit">가입하기</button>
    </form>
  );
};

export default Join;
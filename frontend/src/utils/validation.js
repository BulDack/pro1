// src/utils/validation.js

/**
 * 이메일 형식을 검사하는 유틸리티 함수
 */
export const validateEmail = (email) => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email);
};

/**
 * 비밀번호 길이를 검사하는 유틸리티 함수 (8자 이상)
 */
export const validatePassword = (password) => {
  return password && password.length >= 8;
};
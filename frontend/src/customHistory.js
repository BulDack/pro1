// 컴포넌트 외부에서 라우팅을 조작할 수 있게(react화면을 담당하는 컴포넌트가 아닌 일반 javaScript코드에서 url을 바꾸는것)
// 도와주는 네비게이터 객체임. useNavigate()는 React Router가 관리하는 React Hook이기 때문에 customHistory를 사용하는것
export const customHistory = {
  navigate: null,

  // ✅ page 뒤에 state 인자를 추가하여 원래 페이지 정보 등을 넘길 수 있게 합니다.
  push(page, state = null) {
    // 💡 this 대신 customHistory 객체를 직접 가리켜서 바인딩 에러를 완벽히 차단합니다.
    if (customHistory.navigate) {
      customHistory.navigate(page, {
        replace: true,
        state: state // 원래 보던 페이지 주소 등의 데이터를 담아 보낼 수 있음
      });
    } else {
      window.location.href = page;
    }
  }
};

/* 예를 들어
    Axios 401 발생
         ↓
   axiosConfig.js
         ↓
   customHistory.navigate('/login')
         ↓
   React Router
         ↓
   LoginPage */
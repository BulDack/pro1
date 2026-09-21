// 컴포넌트 외부에서 라우팅을 조작할 수 있게 도와주는 네비게이터 객체입니다.
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
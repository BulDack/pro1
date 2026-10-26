import React from 'react';
import {RouterProvider} from 'react-router-dom'
// 1. 컴포넌트 밖(Axios)에서도 화면 이동이 가능하게 해주는 도구 가져오기
import {AxiosAuthInterceptor} from './components/AxiosAuthInterceptor';
import {AuthProvider} from "@/context/AuthContext.jsx";
import {router} from './routes';//import 경로를 ./routes로만 지정하면, 자바스크립트 번들러(Vite, Webpack 등)는 해당 폴더 안의 index.jsx (또는 index.js) 파일을 기본적으로 찾아 불러옴

//이전과 달리 복잡한 라우팅 <Route> 태그가 존재하지 않으며, 전역 Provider 설정 및 RouterProvider 연결 역할만 수행하게 됨




// 2. 뼈대 컴포넌트
// 부모컴포넌트는 자식컴포넌트를 감싸고,자식 컴포넌트는 부모가 제공하는 Context를 사용할 수 있음.
//부모 컴포넌트가 리렌더링 되면 자식도 강제로 자동으로 리렌더링됨,그반대는 아님!
// 따라서 RouterProvider 내부에서 렌더링되는 MyPage, MainPage 등은 모두 AuthProvider의 인증 상태를 사용할 수 있는 것
function App() {
  return (
      <AuthProvider>          {/* 1층: 인증 상태 저장소 (Context) */}
          <AxiosAuthInterceptor>  {/* 2층: 리액트 상태와 Axios를 이어주는 다리 (Bridge) */}
              <RouterProvider router={router} />   {/* 3층: 실제 화면(페이지)들을 그려주는 라우터 */}
          </AxiosAuthInterceptor>
      </AuthProvider>
  );
}

export default App

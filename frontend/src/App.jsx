import React from 'react';
import {RouterProvider} from 'react-router-dom'
// 1. 컴포넌트 밖(Axios)에서도 화면 이동이 가능하게 해주는 도구 가져오기
import {AxiosAuthInterceptor} from './components/AxiosAuthInterceptor';
import {AuthProvider} from "@/context/AuthContext.jsx";
import {router} from './routes';//import 경로를 ./routes로만 지정하면, 자바스크립트 번들러(Vite, Webpack 등)는 해당 폴더 안의 index.jsx (또는 index.js) 파일을 기본적으로 찾아 불러옴

//이전과 달리 복잡한 라우팅 <Route> 태그가 존재하지 않으며, 전역 Provider 설정 및 RouterProvider 연결 역할만 수행하게 됨




// 2. 뼈대 컴포넌트
function App() {
  return (
      <AuthProvider>
          <AxiosAuthInterceptor>
              <RouterProvider router={router} />
          </AxiosAuthInterceptor>
      </AuthProvider>
  );
}

export default App

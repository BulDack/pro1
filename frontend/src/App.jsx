import { useEffect } from 'react'
import { BrowserRouter, Routes, Route, useNavigate,useSearchParams,Link } from 'react-router-dom'
import './App.css'

// 1. 컴포넌트 밖(Axios)에서도 화면 이동이 가능하게 해주는 도구 가져오기
import { customHistory } from './customHistory'

// 2. 페이지 파일 경로
import LoginPage from './pages/LoginPage';
import Join from './pages/Join';
import PostList from "./pages/PostList";
import PostDetail from "./pages/PostDetail";
import PostCreate from "./pages/PostCreate";



// 4. Axios 인터셉터와 리액트 라우터를 연결해주는 징검다리
function HistoryNavigator() {
  const navigate = useNavigate();
  useEffect(() => {
    customHistory.navigate = navigate;
  }, [navigate]);
  return null;
}


// 5. 뼈대 컴포넌트
function App() {
  return (
    <BrowserRouter>
      {/* 이 징검다리가 있어서 axiosConfig.js에서 401 에러 시 로그인 페이지로 강제 배달이 가능합니다! */}
      <HistoryNavigator />

      <Routes>
        {/* 주소창에 아무것도 안 붙었을 때 (http://localhost:5173/) */}
        <Route path="/" element={<HomePage />} />

        {/* 주소창 뒤에 /login이 붙었을 때 (http://localhost:5173/login) */}
        {/* 💡 방금 보여주신 그 LoginPage 컴포넌트가 여기에 꽂히는 겁니다! */}
        <Route path="/login" element={<LoginPage />} />

        <Route path="/join" element={<Join />} />


      </Routes>

    </BrowserRouter>
  );
}

export default App

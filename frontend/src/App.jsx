import { useEffect } from 'react'
import { BrowserRouter, Routes, Route, useNavigate,useSearchParams,Link } from 'react-router-dom'
import './App.css'

// 1. 컴포넌트 밖(Axios)에서도 화면 이동이 가능하게 해주는 도구 가져오기
import { customHistory } from './customHistory'

// 2. 페이지 파일 경로
import LoginPage from './pages/auth/LoginPage';
import Join from './pages/auth/Join';
import PostList from "./pages/post/PostList";
import PostDetail from "./pages/post/PostDetail";
import PostCreate from "./pages/post/PostCreate";

// 임시 HomePage 컴포넌트 (파일이 따로 있다면 import ./pages/... 경로로 대체하세요)
function HomePage() {
    return (
        <div style={{ padding: '20px' }}>
            <h2>🏠 홈 화면입니다</h2>
            <nav>
                <Link to="/login" style={{ marginRight: '10px' }}>[로그인 페이지 이동]</Link>
                <Link to="/join">[회원가입 페이지 이동]</Link>
            </nav>
        </div>
    );
}

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

          {/* 게시글 관련 경로 추가 예시 */}
          <Route path="/posts" element={<PostList />} />
          <Route path="/posts/:id" element={<PostDetail />} />
          <Route path="/posts/create" element={<PostCreate />} />

          {/* 정의되지 않은 경로 접근 시 404/홈으로 처리 */}
          <Route path="*" element={<HomePage />} />
      </Routes>

    </BrowserRouter>
  );
}

export default App

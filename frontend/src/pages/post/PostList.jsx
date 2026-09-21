import { useEffect, useState } from "react";
import { getPosts } from "../api/postApi";
import { useNavigate } from "react-router-dom";

//   React
//    ↓
//   sort 변경
//    ↓
//   useEffect 실행
//    ↓
//   GET /api/posts?page=0&size=10&sort=title,asc
//    ↓
//   Spring Controller
//    ↓
//   Service
//    ↓
//   QueryDSL
//    ↓
//   MySQL
//    ↓
//   JSON
//    ↓
//   React

function PostList() {

    const [posts, setPosts] = useState([]);
    const [page, setPage] = useState(0);
    const [sort, setSort] = useState("createdDate,desc");

    const navigate = useNavigate();

//페이지가 바뀌거나 정렬 조건이 바뀌면 자동으로 서버에 다시 요청
    useEffect(() => {
        loadPosts();
    }, [page, sort]);

    const loadPosts = async () => {
        try {
            const data = await getPosts(page, sort);

            setPosts(data.content);

        } catch (error) {
            console.error(error);
        }
    };

    const handleSortChange = (e) => {
        setSort(e.target.value);
        setPage(0);
    };

    return (
        <div>
        {/* JSX 태그 내부의 주석입니다 (가장 흔하게 사용) */}
            <h1>게시판</h1>

            <select
                value={sort}
                onChange={handleSortChange}
            >
                <option value="createdDate,desc">
                    최신순
                </option>

                <option value="createdDate,asc">
                    오래된순
                </option>

                <option value="title,asc">
                    제목순
                </option>

                <option value="viewCount,desc">
                    조회수순
                </option>
            </select>

            <table>

                <thead>
                <tr>
                    <th>번호</th>
                    <th>제목</th>
                    <th>작성자</th>
                    <th>조회수</th>
                    <th>작성일</th>
                </tr>
                </thead>

                <tbody>

                {posts.map((post) => (

                    <tr
                        key={post.id}
                        onClick={() => navigate(`/posts/${post.id}`)}
                    >
                        <td>{post.id}</td>
                        <td>{post.title}</td>
                        <td>{post.likeCount}</td>
                        <td>{post.memberLoginId}</td>
                        <td>{post.createdDate}</td>
                    </tr>

                ))}

                </tbody>

            </table>

            <button
                disabled={page === 0}
                onClick={() => setPage(page - 1)}
            >
                이전
            </button>

            <span>{page + 1}</span>

            <button
                onClick={() => setPage(page + 1)}
            >
                다음
            </button>

        </div>
    );
}

export default PostList;
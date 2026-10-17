import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { getPosts } from "@/api/postApi";
import "./PostList.css";

function PostList() {
    const [posts, setPosts] = useState([]);//현재 페이지에 표시할 게시글 배열
    const [page, setPage] = useState(0);// 서버에 요청할 페이지 번호
    const [totalPages, setTotalPages] = useState(0); //검색조건에 해당하는 전체 페이지 수
    const [totalElements, setTotalElements] = useState(0); //검색조건에 해당하는 전체 게시글수
    const [sort, setSort] = useState("LATEST"); //정렬방식

    // 입력 중인 검색어와 실제 조회에 적용된 검색어를 분리한다.
    // 입력할 때마다 서버를 호출하지 않고 검색 버튼/Enter로 제출할 때 조회한다.
    const [searchInput, setSearchInput] = useState(""); //검색창에 현재 입력중인 내용
    const [keyword, setKeyword] = useState(""); //검색버튼을 눌러 실제 조회에 적용한 검색어

    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [searchError, setSearchError] = useState("");

    useEffect(() => {
        // 검색·정렬·페이지를 빠르게 변경했을 때 이전 요청의 응답이
        // 나중에 도착해서 최신 목록을 덮어쓰지 않도록 방지한다.
        let active = true;

        async function loadPosts() {
            setLoading(true);
            setError("");

            try {
                const data = await getPosts(page, sort, keyword);

                if (!active) return;

                setPosts(data.content ?? []);
                setTotalPages(data.totalPages ?? 0);
                setTotalElements(data.totalElements ?? 0);
            } catch (err) {
                if (!active) return;

                console.error("게시글 조회 실패:", err);
                setError("게시글을 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.");
            } finally {
                if (active) {
                    setLoading(false);
                }
            }
        }

        loadPosts();

        // 조회 조건이 바뀌거나 화면을 떠나면 이전 요청의 결과를 무시한다.
        return () => {
            active = false;
        };
    }, [page, sort, keyword]);

    const handleSearch = (event) => {
        event.preventDefault();

        const trimmedKeyword = searchInput.trim();

        // 서버에서는 2글자 미만의 검색어를 검색 조건에서 제외한다.
        // 사용자가 전체 목록을 검색 결과로 오해하지 않도록 제출을 막는다.
        // 빈 문자열 제출은 전체 목록 조회로 처리한다.
        if (trimmedKeyword && trimmedKeyword.length < 2) {
            setSearchError("검색어는 2글자 이상 입력해 주세요.");
            return;
        }

        setSearchError("");
        setSearchInput(trimmedKeyword);
        setKeyword(trimmedKeyword);

        // 검색 조건이 변경되면 첫 페이지부터 결과를 보여준다.
        setPage(0);
    };

    const handleReset = () => {
        setSearchInput("");
        setKeyword("");
        setSearchError("");
        setPage(0);
    };

    const handleSortChange = (event) => {
        setSort(event.target.value);
        setPage(0);
    };

    return (
        <section className="post-list" aria-labelledby="post-list-heading">
            <header className="post-list__header">
                <div>
                    <h1 id="post-list-heading">게시판</h1>
                    <p>자유롭게 이야기를 나누고 정보를 공유해 보세요.</p>
                </div>

                {/* 기존 작성 화면으로 이동한다.
                    로그인 여부는 /posts/new의 기존 인증 가드가 처리한다. */}
                <Link
                    to="/posts/new"
                    className="post-list__button post-list__button--primary"
                >
                    <span aria-hidden="true">＋</span>
                    글쓰기
                </Link>
            </header>

            <div className="post-list__toolbar">
                {/* form을 사용하므로 검색 버튼과 Enter가 같은 동작을 수행한다. */}
                <form
                    className="post-list__search"
                    onSubmit={handleSearch}
                    role="search"
                    aria-label="게시글 제목 검색"
                >
                    <label className="post-list__sr-only" htmlFor="post-search">
                        검색할 게시글 제목
                    </label>

                    <input
                        id="post-search"
                        type="search"
                        placeholder="제목으로 검색하세요"
                        value={searchInput}
                        onChange={(event) => {
                            setSearchInput(event.target.value);
                            setSearchError("");
                        }}
                        aria-invalid={Boolean(searchError)}
                        aria-describedby={
                            searchError ? "post-search-error" : undefined
                        }
                    />

                    <button
                        type="submit"
                        className="post-list__button post-list__button--dark"
                    >
                        검색
                    </button>

                    {(searchInput || keyword) && (
                        <button
                            type="button"
                            className="post-list__button post-list__button--outline"
                            onClick={handleReset}
                        >
                            초기화
                        </button>
                    )}
                </form>

                <div className="post-list__sort">
                    <label className="post-list__sr-only" htmlFor="post-sort">
                        게시글 정렬
                    </label>

                    <select
                        id="post-sort"
                        value={sort}
                        onChange={handleSortChange}
                    >
                        <option value="LATEST">최신순</option>
                        <option value="OLDEST">오래된순</option>
                        <option value="TITLE">제목순</option>
                        <option value="POPULAR">좋아요순</option>
                    </select>
                </div>
            </div>

            {searchError && (
                <p
                    id="post-search-error"
                    className="post-list__error"
                    role="alert"
                >
                    {searchError}
                </p>
            )}

            {error && (
                <p className="post-list__error" role="alert">
                    {error}
                </p>
            )}

            <div className="post-list__summary" aria-live="polite">
                {loading ? (
                    "게시글을 불러오는 중입니다."
                ) : error ? (
                    "목록을 불러오지 못했습니다."
                ) : (
                    <>
                        {keyword && (
                            <span className="post-list__keyword">
                                “{keyword}” 검색 결과 ·{" "}
                            </span>
                        )}
                        총 <strong>{totalElements.toLocaleString()}</strong>개의
                        게시글
                    </>
                )}
            </div>

            <div className="post-list__table-wrap" aria-busy={loading}>
                <table className="post-list__table">
                    <caption className="post-list__sr-only">
                        게시글 목록
                    </caption>

                    <thead>
                    <tr>
                        <th scope="col">번호</th>
                        <th scope="col">제목</th>
                        <th scope="col">작성자</th>
                        <th scope="col">조회수</th>
                        <th scope="col">작성일</th>
                    </tr>
                    </thead>

                    <tbody>
                    {loading ? (
                        <tr>
                            <td colSpan={5} className="post-list__empty">
                                게시글을 불러오는 중입니다.
                            </td>
                        </tr>
                    ) : error ? (
                        <tr>
                            <td colSpan={5} className="post-list__empty">
                                목록을 표시할 수 없습니다.
                            </td>
                        </tr>
                    ) : posts.length === 0 ? (
                        <tr>
                            <td colSpan={5} className="post-list__empty">
                                {keyword
                                    ? "검색 결과가 없습니다."
                                    : "아직 등록된 게시글이 없습니다."}
                            </td>
                        </tr>
                    ) : (
                        posts.map((post) => (
                            <tr key={post.id}>
                                <td>{post.id}</td>

                                <td className="post-list__title">
                                    {/* 링크를 사용해 키보드 접근과
                                            새 탭으로 열기도 지원한다. */}
                                    <Link to={`/posts/${post.id}`}>
                                        {post.title}
                                    </Link>
                                </td>

                                {/* 서버의 목록 응답 필드에 맞춰 표시한다. */}
                                <td>{post.nickname ?? "-"}</td>
                                <td>{post.viewCount ?? 0}</td>
                                <td>
                                    {post.createdDate
                                        ? post.createdDate
                                            .slice(0, 10)
                                            .replaceAll("-", ".")
                                        : "-"}
                                </td>
                            </tr>
                        ))
                    )}
                    </tbody>
                </table>
            </div>

            <nav className="post-list__pagination" aria-label="게시글 페이지">
                <button
                    type="button"
                    className="post-list__button post-list__button--outline"
                    disabled={loading || Boolean(error) || page === 0}
                    onClick={() => setPage((current) => current - 1)}
                >
                    이전
                </button>

                <span className="post-list__page" aria-live="polite">
                    {totalPages > 0 ? page + 1 : 0} / {totalPages}
                </span>

                {/* 서버가 반환한 전체 페이지 수를 기준으로
                    마지막 페이지 이후의 이동을 차단한다. */}
                <button
                    type="button"
                    className="post-list__button post-list__button--outline"
                    disabled={
                        loading ||
                        Boolean(error) ||
                        page + 1 >= totalPages
                    }
                    onClick={() => setPage((current) => current + 1)}
                >
                    다음
                </button>
            </nav>
        </section>
    );
}

export default PostList;
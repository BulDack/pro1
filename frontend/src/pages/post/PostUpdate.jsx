import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { getPost, updatePost } from "@/api/postApi";

// 작성 페이지와 같은 스타일을 사용해
// 카드, 입력창, 버튼, 모바일 화면의 디자인을 통일한다.
import "./PostCreate.css";

function PostUpdate() {
    const { postId } = useParams();
    const navigate = useNavigate();

    const [title, setTitle] = useState("");
    const [content, setContent] = useState("");

    // 기존 게시글을 불러오기 전에는 입력과 저장을 막는다.
    const [loading, setLoading] = useState(true);

    // 조회 실패 시 빈 폼이 저장되는 것을 방지한다.
    const [loadFailed, setLoadFailed] = useState(false);

    // 저장 중 중복 제출을 방지하고 버튼 문구를 변경한다.
    const [submitting, setSubmitting] = useState(false);

    // 조회 실패, 입력 오류, 저장 실패를 화면에 표시한다.
    const [error, setError] = useState("");

    useEffect(() => {
        // 다른 게시글로 이동하거나 화면을 벗어난 뒤
        // 이전 요청의 응답이 현재 화면에 반영되지 않도록 한다.
        let cancelled = false;

        const loadPost = async () => {
            setLoading(true);
            setLoadFailed(false);
            setError("");
            setTitle("");
            setContent("");

            try {
                const post = await getPost(postId);

                if (cancelled) return;

                // 값이 없더라도 입력창에는 항상 문자열을 전달한다.
                setTitle(post.title ?? "");
                setContent(post.content ?? "");
            } catch (err) {
                if (cancelled) return;

                console.error("게시글 조회 실패:", err);
                setLoadFailed(true);
                setError("게시글을 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.");
            } finally {
                if (!cancelled) {
                    setLoading(false);
                }
            }
        };

        loadPost();

        return () => {
            cancelled = true;
        };
    }, [postId]);

    const handleSubmit = async (event) => {
        event.preventDefault();

        // 조회가 끝나지 않았거나 저장 중인 경우에는 요청하지 않는다.
        if (loading || loadFailed || submitting) return;

        // 공백만 입력한 제목과 내용은 저장하지 않는다.
        if (!title.trim() || !content.trim()) {
            setError("제목과 내용을 입력해 주세요.");
            return;
        }

        setSubmitting(true);
        setError("");

        try {
            // 기존 수정 API의 요청 형식을 유지한다.
            // 본문은 사용자가 입력한 줄바꿈과 공백을 보존한다.
            await updatePost(postId, {
                title: title.trim(),
                content,
            });

            // 수정이 완료되면 해당 게시글 상세 화면으로 이동한다.
            navigate(`/posts/${postId}`);
        } catch (err) {
            console.error("게시글 수정 실패:", err);

            // 실패해도 작성 중인 내용은 유지해 다시 저장할 수 있게 한다.
            setError("게시글을 수정하지 못했습니다. 잠시 후 다시 시도해 주세요.");
        } finally {
            setSubmitting(false);
        }
    };

    const formDisabled = loading || loadFailed || submitting;

    return (
        <section
            className="post-create"
            aria-labelledby="post-update-heading"
        >
            <header className="post-create__header">
                <h1 id="post-update-heading">게시글 수정</h1>
                <p>수정할 내용을 확인한 뒤 변경 사항을 저장해 주세요.</p>
            </header>

            <form
                className="post-create__form"
                onSubmit={handleSubmit}
                aria-busy={loading || submitting}
            >
                {/* 게시글 조회 상태를 보조 기술에도 전달한다. */}
                {loading && (
                    <p role="status">게시글을 불러오는 중입니다...</p>
                )}

                <div className="post-create__field">
                    <label htmlFor="post-update-title">제목</label>

                    <input
                        id="post-update-title"
                        name="title"
                        type="text"
                        value={title}
                        onChange={(event) => setTitle(event.target.value)}
                        placeholder="게시글 제목을 입력해 주세요"
                        disabled={formDisabled}
                        required
                    />
                </div>

                <div className="post-create__field">
                    <label htmlFor="post-update-content">내용</label>

                    <textarea
                        id="post-update-content"
                        name="content"
                        value={content}
                        onChange={(event) => setContent(event.target.value)}
                        placeholder="내용을 입력해 주세요"
                        disabled={formDisabled}
                        required
                        aria-describedby="post-update-count"
                    />

                    <p
                        id="post-update-count"
                        className="post-create__count"
                    >
                        {content.length.toLocaleString()}자
                    </p>
                </div>

                {/* 오류가 발생해도 화면을 이동하지 않고 폼 안에서 안내한다. */}
                {error && (
                    <p className="post-create__error" role="alert">
                        {error}
                    </p>
                )}

                <footer className="post-create__footer">
                    {/* 취소 버튼이 폼 제출을 실행하지 않도록
                        type="button"을 지정한다. */}
                    <button
                        type="button"
                        className="post-create__button post-create__button--outline"
                        onClick={() => navigate(`/posts/${id}`)}
                        disabled={submitting}
                    >
                        취소
                    </button>

                    <button
                        type="submit"
                        className="post-create__button post-create__button--primary"
                        disabled={formDisabled}
                    >
                        {submitting ? "저장 중..." : "수정 완료"}
                    </button>
                </footer>
            </form>
        </section>
    );
}

export default PostUpdate;
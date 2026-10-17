import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { createPost } from "@/api/postApi";
import "./PostCreate.css";

function PostCreate() {
    const navigate = useNavigate();

    const [title, setTitle] = useState("");
    const [content, setContent] = useState("");

    // 등록 요청 중에는 버튼을 비활성화하고 진행 상태를 표시한다.
    const [submitting, setSubmitting] = useState(false);

    // 등록 실패 또는 입력 오류를 폼 안에 표시한다.
    const [error, setError] = useState("");

    const handleSubmit = async (event) => {
        // form 제출로 페이지가 새로고침되는 것을 막는다.
        event.preventDefault();

        if (submitting) return;

        // 공백만 입력한 제목이나 내용은 등록하지 않는다.
        if (!title.trim() || !content.trim()) {
            setError("제목과 내용을 입력해 주세요.");
            return;
        }

        setSubmitting(true);
        setError("");

        try {
            // 기존 API의 요청 형식과 등록 후 이동 경로를 유지한다.
            const postId = await createPost({
                title: title.trim(),
                content,
            });

            navigate(`/posts/${postId}`);
        } catch (err) {
            console.error("게시글 등록 실패:", err);

            // 요청이 실패해도 입력한 제목과 내용은 그대로 유지한다.
            setError(
                "게시글을 등록하지 못했습니다. 잠시 후 다시 시도해 주세요."
            );
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <section
            className="post-create"
            aria-labelledby="post-create-heading"
        >
            <header className="post-create__header">
                <h1 id="post-create-heading">게시글 작성</h1>
                <p>나누고 싶은 이야기나 정보를 자유롭게 작성해 주세요.</p>
            </header>

            <form
                className="post-create__form"
                onSubmit={handleSubmit}
                aria-busy={submitting}
            >
                {/* 제목 입력 영역 */}
                <div className="post-create__field">
                    <label htmlFor="post-title">제목</label>

                    <input
                        id="post-title"
                        name="title"
                        type="text"
                        value={title}
                        onChange={(event) => setTitle(event.target.value)}
                        placeholder="게시글 제목을 입력해 주세요"
                        disabled={submitting}
                        required
                    />
                </div>

                {/* 내용 입력 영역.
                    textarea는 세로 크기를 사용자가 조절할 수 있다. */}
                <div className="post-create__field">
                    <label htmlFor="post-content">내용</label>

                    <textarea
                        id="post-content"
                        name="content"
                        value={content}
                        onChange={(event) => setContent(event.target.value)}
                        placeholder="내용을 입력해 주세요"
                        disabled={submitting}
                        required
                    />

                    <p className="post-create__count">
                        {content.length.toLocaleString()}자
                    </p>
                </div>

                {error && (
                    <p className="post-create__error" role="alert">
                        {error}
                    </p>
                )}

                <footer className="post-create__footer">
                    {/* form 안의 보조 버튼은 type="button"으로 지정해
                        등록 요청이 실행되지 않도록 한다. */}
                    <button
                        type="button"
                        className="post-create__button post-create__button--outline"
                        onClick={() => navigate("/posts")}
                        disabled={submitting}
                    >
                        목록으로
                    </button>

                    <button
                        type="submit"
                        className="post-create__button post-create__button--primary"
                        disabled={submitting}
                    >
                        {submitting ? "등록 중..." : "게시글 등록"}
                    </button>
                </footer>
            </form>
        </section>
    );
}

export default PostCreate;
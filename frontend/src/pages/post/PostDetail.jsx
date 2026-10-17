import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import {
    getPost,
    deletePost,
    likePost,
    unlikePost,
} from "@/api/postApi";
import "./PostDetail.css";

function PostDetail() {
    const { postId } = useParams();
    const navigate = useNavigate();

    const [post, setPost] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    // 좋아요·삭제 요청 중 버튼을 비활성화하기 위한 상태.
    // 요청 종류에 따라 버튼의 안내 문구도 다르게 표시한다.
    const [pendingAction, setPendingAction] = useState("");
    const [actionError, setActionError] = useState("");

    useEffect(() => {
        // 다른 게시글로 이동하거나 화면을 떠난 뒤,
        // 이전 조회 응답이 현재 화면에 반영되는 것을 방지한다.
        let active = true;

        const controller=new AbortController();

        async function loadPost() {
            setLoading(true);
            setError("");
            setActionError("");
            setPost(null);

            try {
                const data = await getPost(postId,{signal: controller.signal});

                if (!data) {
                    throw new Error("게시글 데이터가 없습니다.");
                }

                if (active) {
                    setPost(data);
                }
            } catch (err) {
                if (!active) return;

                console.error("게시글 조회 실패:", err);
                setError("게시글을 불러오지 못했습니다.");
            } finally {
                if (active) {
                    setLoading(false);
                }
            }
        }

        loadPost();

        return () => {
            active = false;
            controller.abort();
        };
    }, [postId]);

    const handleDelete = async () => {
        if (!post?.canDelete || pendingAction) return;

        if (!window.confirm("게시글을 삭제하시겠습니까?")) return;

        setPendingAction("delete");
        setActionError("");

        try {
            await deletePost(postId);
            navigate("/posts", { replace: true });
        } catch (err) {
            console.error("게시글 삭제 실패:", err);
            setActionError("게시글을 삭제하지 못했습니다.");
        } finally {
            setPendingAction("");
        }
    };

    const handleLike = async () => {
        if (!post || pendingAction) return;

        setPendingAction("like");
        setActionError("");

        try {
            // 서버에서 받은 현재 좋아요 상태에 따라
            // 좋아요 등록 또는 취소 API를 호출한다.
            if (post.liked) {
                await unlikePost(postId);
            } else {
                await likePost(postId);
            }
        } catch (err) {
            console.error("좋아요 처리 실패:", err);
            setActionError(
                "좋아요를 처리하지 못했습니다. 로그인 상태를 확인해 주세요."
            );
            setPendingAction("");
            return;
        }

        try {
            // 변경 후 서버의 최신 좋아요 수와 상태를 다시 받아 표시한다.
            const updatedPost = await getPost(postId);

            if (!updatedPost) {
                throw new Error("게시글 데이터가 없습니다.");
            }

            setPost(updatedPost);
        } catch (err) {
            console.error("좋아요 상태 조회 실패:", err);

            // 좋아요 변경은 성공했지만 재조회가 실패한 경우를 구분한다.
            setActionError(
                "좋아요는 처리했지만 최신 상태를 불러오지 못했습니다. 새로고침해 주세요."
            );
        } finally {
            setPendingAction("");
        }
    };

    // 서버의 LocalDateTime 문자열에서 날짜와 시·분만 표시한다.
    // 시간대 변환 없이 전달받은 문자열을 정리하는 방식이다.
    const formattedDate = post?.createdDate
        ? post.createdDate
            .slice(0, 16)
            .replace("T", " ")
            .replaceAll("-", ".")
        : "-";

    const busy = pendingAction !== "";

    return (
        <section
            className="post-detail"
            aria-labelledby="post-detail-heading"
        >
            <header className="post-detail__page-header">
                <h1 id="post-detail-heading">게시글</h1>
                <p>함께 나누는 이야기와 정보를 확인해 보세요.</p>
            </header>

            {loading ? (
                <div
                    className="post-detail__feedback"
                    role="status"
                >
                    게시글을 불러오는 중입니다.
                </div>
            ) : error || !post ? (
                <div className="post-detail__feedback">
                    <p role="alert">
                        {error || "게시글을 찾을 수 없습니다."}
                    </p>

                    <button
                        type="button"
                        className="post-detail__button post-detail__button--outline"
                        onClick={() => navigate("/posts")}
                    >
                        목록으로
                    </button>
                </div>
            ) : (
                <article className="post-detail__card">
                    <header className="post-detail__article-header">
                        <span className="post-detail__badge">
                            게시판
                        </span>

                        <h2>{post.title}</h2>

                        <dl className="post-detail__meta">
                            <div>
                                <dt>작성자</dt>
                                <dd>{post.nickname ?? "-"}</dd>
                            </div>

                            <div>
                                <dt>작성일</dt>
                                <dd>
                                    <time
                                        dateTime={
                                            post.createdDate || undefined
                                        }
                                    >
                                        {formattedDate}
                                    </time>
                                </dd>
                            </div>

                            <div>
                                <dt>조회수</dt>
                                <dd>{post.viewCount ?? 0}</dd>
                            </div>
                        </dl>
                    </header>

                    {/* 일반 텍스트로 출력해 HTML이 실행되지 않도록 한다.
                        줄바꿈과 긴 문장의 처리는 CSS에서 담당한다. */}
                    <div className="post-detail__content">
                        {post.content}
                    </div>

                    <div className="post-detail__reaction">
                        <button
                            type="button"
                            className={`post-detail__like ${
                                post.liked
                                    ? "post-detail__like--active"
                                    : ""
                            }`}
                            onClick={handleLike}
                            disabled={busy}
                            aria-pressed={Boolean(post.liked)}
                        >
                            <span aria-hidden="true">
                                {post.liked ? "♥" : "♡"}
                            </span>

                            <span>
                                {pendingAction === "like"
                                    ? "처리 중..."
                                    : post.liked
                                        ? "좋아요 취소"
                                        : "좋아요"}
                            </span>

                            <strong>{post.likeCount ?? 0}</strong>
                        </button>
                    </div>

                    {actionError && (
                        <p
                            className="post-detail__error"
                            role="alert"
                        >
                            {actionError}
                        </p>
                    )}

                    <footer className="post-detail__footer">
                        <button
                            type="button"
                            className="post-detail__button post-detail__button--outline"
                            onClick={() => navigate("/posts")}
                            disabled={busy}
                        >
                            목록으로
                        </button>

                        {/* 서버가 내려준 권한 정보로 버튼 노출을 결정한다.
                            실제 수정·삭제 권한 검사는 서버에서도 수행해야 한다. */}
                        <div className="post-detail__actions">
                            {post.canEdit && (
                                <button
                                    type="button"
                                    className="post-detail__button post-detail__button--primary"
                                    onClick={() =>
                                        navigate(`/posts/${postId}/edit`)
                                    }
                                    disabled={busy}
                                >
                                    수정
                                </button>
                            )}

                            {post.canDelete && (
                                <button
                                    type="button"
                                    className="post-detail__button post-detail__button--danger"
                                    onClick={handleDelete}
                                    disabled={busy}
                                >
                                    {pendingAction === "delete"
                                        ? "삭제 중..."
                                        : "삭제"}
                                </button>
                            )}
                        </div>
                    </footer>
                </article>
            )}
        </section>
    );
}

export default PostDetail;
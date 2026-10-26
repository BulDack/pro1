import { useEffect, useId, useRef, useState } from "react";
import {
    getComment,
    createComment,
    deleteComment,
} from "@/api/postApi.js";
import { useAuth } from "@/context/AuthContext";
import "./PostComment.css";

const MAX_LENGTH = 500;

// 서버의 LocalDateTime을 시간대 변환 없이 화면에 표시한다.
function formatDate(value) {
    if (!value) return "-";

    return value
        .slice(0, 16)
        .replace("T", " ")
        .replaceAll("-", ".");
}

function PostComment({ postId } ) {
    const fieldId = useId();

    const { accessToken, isLoading: authLoading } = useAuth();
    const isLoggedIn = Boolean(accessToken);

    const [comments, setComments] = useState([]);
    const [content, setContent] = useState("");

    // 서버의 페이지 번호는 0부터 시작한다.
    const [page, setPage] = useState(0);
    const [sort, setSort] = useState("LATEST");
    const [totalPages, setTotalPages] = useState(0);
    const [totalElements, setTotalElements] = useState(0);

    const [loading, setLoading] = useState(true);
    const [loadError, setLoadError] = useState("");
    const [writeError, setWriteError] = useState("");
    const [actionError, setActionError] = useState("");
    const [notice, setNotice] = useState("");

    // 진행 중인 작업을 구분해 버튼과 입력창을 비활성화한다.
    const [pendingAction, setPendingAction] = useState("");

    // 상태가 화면에 반영되기 전 발생하는 연속 제출도 방지한다.
    const actionLock = useRef(false);

    // 작성·삭제·재시도 후 같은 페이지를 다시 조회하기 위한 값.
    const [reloadKey, setReloadKey] = useState(0);

    const busy = pendingAction !== "";

    useEffect(() => {
        // 페이지 이동 등으로 오래된 요청이 늦게 도착해도
        // 현재 댓글 목록을 덮어쓰지 않도록 한다.
        let active = true;

        async function loadComments() {
            setLoading(true);
            setLoadError("");
            setComments([]);

            try {
                if (postId == null || postId === "") {
                    throw new Error("게시글 ID가 없습니다.");
                }

                /*
                 * 댓글 조회는 로그인 여부와 관계없이 실행한다.
                 * getComment는 ApiResponse.data인 Page 객체를 반환한다.
                 */
                const result = await getComment(postId, page, sort);

                if (!active) return;

                if (!result || !Array.isArray(result.content)) {
                    throw new Error("댓글 응답 형식이 올바르지 않습니다.");
                }

                /*
                 * 마지막 페이지의 마지막 댓글을 삭제하면
                 * 현재 페이지가 사라질 수 있으므로 이전 페이지로 이동한다.
                 */
                const lastPage = Math.max(0, result.totalPages - 1);

                if (page > lastPage) {
                    setPage(lastPage);
                    return;
                }

                setComments(result.content);
                setTotalPages(result.totalPages);
                setTotalElements(result.totalElements);
            } catch (err) {
                if (!active) return;

                console.error("댓글 조회 실패:", err);
                setLoadError(
                    "댓글을 불러오지 못했습니다. 잠시 후 다시 시도해 주세요."
                );
            } finally {
                if (active) {
                    setLoading(false);
                }
            }
        }

        loadComments();

        return () => {
            active = false;
        };
    }, [postId, page, sort, reloadKey]);

    // 입력한 댓글이 서버에 저장된 이후에만 입력창을 초기화한다.
    const handleCreate = async (event) => {
        event.preventDefault();

        //이미 이 컴포넌트에서 작업이 진행 중이면 종료
        if (actionLock.current) return;

        if (authLoading || !isLoggedIn) {
            setWriteError("댓글을 작성하려면 로그인해 주세요.");
            return;
        }

        if (postId == null || postId === "") {
            setWriteError("게시글 ID가 없습니다.");
            return;
        }

        const trimmedContent = content.trim();

        if (!trimmedContent) {
            setWriteError("댓글 내용을 입력해 주세요.");
            return;
        }

        if (content.length > MAX_LENGTH) {
            setWriteError(`댓글은 ${MAX_LENGTH}자 이내로 작성해 주세요.`);
            return;
        }

        actionLock.current = true;
        setPendingAction("create");
        setWriteError("");
        setActionError("");
        setNotice("");

        try {
            /*
             * DTO에 맞춰 문자열이 아닌 객체를 전송한다.
             * 작성자 ID는 서버에서 인증 정보로 결정한다.
             */
            await createComment(postId, {
                content: trimmedContent,
            });

            /*
             * 등록 응답은 댓글 ID만 반환하므로 목록에 직접 추가하지 않는다.
             * 최신순 첫 페이지를 재조회해 서버의 작성자·작성일을 표시한다.
             */
            setContent("");
            setSort("LATEST");
            setPage(0);
            setReloadKey((previous) => previous + 1);
            setNotice("댓글이 등록되었습니다.");
        } catch (err) {
            console.error("댓글 등록 실패:", err);

            // 실패해도 작성 중인 내용은 유지한다.
            setWriteError(
                err.response?.status === 401
                    ? "댓글을 작성하려면 로그인해 주세요."
                    : "댓글을 등록하지 못했습니다. 잠시 후 다시 시도해 주세요."
            );
        } finally {
            actionLock.current = false;
            setPendingAction("");
        }
    };

    // 현재 로그인 사용자와 댓글 작성자를 비교해 버튼 표시를 결정한다.
    const canDeleteComment = (comment) =>
        !authLoading &&
        isLoggedIn &&
        comment.isAuthor === true;

    const handleDelete = async (comment) => {
        if (actionLock.current || !canDeleteComment(comment)) return;
        if (!window.confirm("댓글을 삭제하시겠습니까?")) return;

        actionLock.current = true;
        setPendingAction(`delete:${comment.id}`);
        setActionError("");
        setNotice("");

        try {
            // 실제 삭제 권한은 백엔드에서도 확인한다.
            await deleteComment(postId, comment.id);

            // 전체 댓글 수와 페이지 정보를 함께 갱신한다.
            setReloadKey((previous) => previous + 1);
            setNotice("댓글이 삭제되었습니다.");
        } catch (err) {
            console.error("댓글 삭제 실패:", err);
            setActionError(
                "댓글을 삭제하지 못했습니다. 로그인 상태와 삭제 권한을 확인해 주세요."
            );
        } finally {
            actionLock.current = false;
            setPendingAction("");
        }
    };

    const changeSort = (nextSort) => {
        if (busy || loading) return;

        setSort(nextSort);
        setPage(0);
    };

    return (
        <section
            className="post-comments"
            aria-labelledby={`${fieldId}-heading`}
        >
            <header className="post-comments__header">
                <div className="post-comments__heading">
                    <h2 id={`${fieldId}-heading`}>댓글</h2>

                    {!loading && !loadError && (
                        <span className="post-comments__badge">
                            {totalElements.toLocaleString()}
                        </span>
                    )}
                </div>

                <p>게시글에 대한 생각을 자유롭게 나눠 주세요.</p>
            </header>

            {/* 로그인 확인과 작성 폼만 분기한다.
                아래 댓글 목록은 비로그인 사용자에게도 표시한다. */}
            {authLoading ? (
                <p className="post-comments__notice" role="status">
                    로그인 상태를 확인하는 중입니다.
                </p>
            ) : isLoggedIn ? (
                <form
                    className="post-comments__composer"
                    onSubmit={handleCreate}
                    aria-busy={pendingAction === "create"}
                >
                    <label htmlFor={`${fieldId}-write`}>댓글 작성</label>

                    <textarea
                        id={`${fieldId}-write`}
                        value={content}
                        onChange={(event) => {
                            setContent(event.target.value);
                            setWriteError("");
                        }}
                        placeholder="서로를 존중하는 따뜻한 댓글을 남겨 주세요."
                        maxLength={MAX_LENGTH}
                        disabled={busy}
                        aria-invalid={Boolean(writeError)}
                        aria-describedby={
                            writeError
                                ? `${fieldId}-write-error`
                                : undefined
                        }
                    />

                    {writeError && (
                        <p
                            id={`${fieldId}-write-error`}
                            className="post-comments__error"
                            role="alert"
                        >
                            {writeError}
                        </p>
                    )}

                    <div className="post-comments__composer-footer">
                        <span className="post-comments__count">
                            {content.length.toLocaleString()} / {MAX_LENGTH}자
                        </span>

                        <button
                            type="submit"
                            className="post-comments__button post-comments__button--primary"
                            disabled={busy}
                        >
                            {pendingAction === "create"
                                ? "등록 중..."
                                : "댓글 등록"}
                        </button>
                    </div>
                </form>
            ) : (
                <div className="post-comments__composer">
                    <p className="post-comments__login-message">
                        댓글을 작성하려면 로그인해 주세요.
                    </p>
                </div>
            )}

            <p className="post-comments__notice" role="status">
                {notice}
            </p>

            {actionError && (
                <p className="post-comments__error" role="alert">
                    {actionError}
                </p>
            )}

            {/* 정렬과 페이지 이동은 비로그인도 사용할 수 있다. */}
            <div
                className="post-comments__sort"
                role="group"
                aria-label="댓글 정렬"
            >
                <button
                    type="button"
                    className="post-comments__text-button"
                    aria-pressed={sort === "LATEST"}
                    disabled={busy || loading}
                    onClick={() => changeSort("LATEST")}
                >
                    최신순
                </button>

                <button
                    type="button"
                    className="post-comments__text-button"
                    aria-pressed={sort === "OLDEST"}
                    disabled={busy || loading}
                    onClick={() => changeSort("OLDEST")}
                >
                    오래된순
                </button>
            </div>

            {loading ? (
                <p className="post-comments__notice" role="status">
                    댓글을 불러오는 중입니다.
                </p>
            ) : loadError ? (
                <div className="post-comments__empty">
                    <p className="post-comments__error" role="alert">
                        {loadError}
                    </p>

                    <button
                        type="button"
                        className="post-comments__button post-comments__button--outline"
                        disabled={busy}
                        onClick={() =>
                            setReloadKey((previous) => previous + 1)
                        }
                    >
                        다시 불러오기
                    </button>
                </div>
            ) : comments.length === 0 ? (
                <div className="post-comments__empty">
                    <strong>아직 댓글이 없습니다.</strong>
                    <p>첫 번째 댓글을 남겨 보세요.</p>
                </div>
            ) : (
                <>
                    <ul className="post-comments__list">
                        {comments.map((comment) => (
                            <li
                                key={comment.id}
                                className="post-comments__item"
                            >
                                <div
                                    className="post-comments__avatar"
                                    aria-hidden="true"
                                >
                                    {Array.from(comment.nickname || "?")[0]}
                                </div>

                                <div className="post-comments__body">
                                    <div className="post-comments__meta">
                                        <strong>
                                            {comment.nickname ?? "알 수 없음"}
                                        </strong>

                                        <time dateTime={comment.createdDate}>
                                            {formatDate(comment.createdDate)}
                                        </time>
                                    </div>

                                    <p className="post-comments__content">
                                        {comment.content}
                                    </p>

                                    {canDeleteComment(comment) && (
                                        <div className="post-comments__actions">
                                            <button
                                                type="button"
                                                className="post-comments__text-button post-comments__text-button--danger"
                                                disabled={busy}
                                                onClick={() =>
                                                    handleDelete(comment)
                                                }
                                            >
                                                {pendingAction ===
                                                `delete:${comment.id}`
                                                    ? "삭제 중..."
                                                    : "삭제"}
                                            </button>
                                        </div>
                                    )}
                                </div>
                            </li>
                        ))}
                    </ul>

                    {totalPages > 1 && (
                        <nav
                            className="post-comments__pagination"
                            aria-label="댓글 페이지"
                        >
                            <button
                                type="button"
                                className="post-comments__button post-comments__button--outline"
                                disabled={busy || page === 0}
                                onClick={() =>
                                    setPage((previous) => previous - 1)
                                }
                            >
                                이전
                            </button>

                            <span className="post-comments__count">
                                {page + 1} / {totalPages}
                            </span>

                            <button
                                type="button"
                                className="post-comments__button post-comments__button--outline"
                                disabled={busy || page >= totalPages - 1}
                                onClick={() =>
                                    setPage((previous) => previous + 1)
                                }
                            >
                                다음
                            </button>
                        </nav>
                    )}
                </>
            )}
        </section>
    );
}

export default PostComment;
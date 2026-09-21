import { useEffect, useState } from "react";
import {
    useNavigate,
    useParams
} from "react-router-dom";

import {
    getPost,
    deletePost,
    likePost,
    unlikePost
} from "../api/postApi";

function PostDetail() {

    const { id } = useParams();

    const navigate = useNavigate();

    const [post, setPost] =
        useState(null);

    useEffect(() => {

        loadPost();

    }, [id]);

    const loadPost = async () => {

        try {

            const data =
                await getPost(id);

            setPost(data);

        } catch (error) {

            console.error(error);
        }
    };

    const handleDelete = async () => {

        if (!window.confirm(
            "삭제하시겠습니까?"
        )) {
            return;
        }

        await deletePost(id);

        navigate("/posts");
    };

    const handleLike = async () => {

        if (post.likedByMe) {

            await unlikePost(id);

        } else {

            await likePost(id);
        }

        await loadPost();
    };

    if (!post) {
        return <div>Loading...</div>;
    }

    return (
        <div>

            <h1>{post.title}</h1>

            <div>
                작성자 : {post.loginId}
            </div>

            <div>
                좋아요수 : {post.likeCount}
            </div>

            <div>
                작성일 : {post.createdDate}
            </div>

            <hr />

            <div>
                {post.content}
            </div>

            <hr />

            <button onClick={handleLike}>

                {post.likedByMe
                    ? "❤️ 좋아요 취소"
                    : "🤍 좋아요"}

            </button>

            <span>
                좋아요 {post.likeCount}
            </span>

            <br />

            <button
                onClick={() =>
                    navigate(`/posts/${id}/edit`)
                }
            >
                수정
            </button>

            <button
                onClick={handleDelete}
            >
                삭제
            </button>

            <button
                onClick={() =>
                    navigate("/posts")
                }
            >
                목록
            </button>

        </div>
    );
}

export default PostDetail;
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { createPost } from "../api/postApi";

function PostCreate() {

    const navigate = useNavigate();

    const [title, setTitle] =
        useState("");

    const [content, setContent] =
        useState("");

    const handleSubmit = async (e) => {

        e.preventDefault();

        const postId =
            await createPost({
                title,
                content
            });

        navigate(`/posts/${postId}`);
    };

    return (
        <form onSubmit={handleSubmit}>

            <h1>게시글 작성</h1>

            <input
                value={title}
                onChange={(e) =>
                    setTitle(e.target.value)
                }
                placeholder="제목"
            />

            <textarea
                value={content}
                onChange={(e) =>
                    setContent(e.target.value)
                }
                placeholder="내용"
            />

            <button type="submit">
                작성
            </button>

        </form>
    );
}

export default PostCreate;
import { useEffect, useState } from "react";
import {
    useNavigate,
    useParams
} from "react-router-dom";

import {
    getPost,
    updatePost
} from "../api/postApi";

function PostUpdate() {

    const { id } = useParams();

    const navigate = useNavigate();

    const [title, setTitle] =
        useState("");

    const [content, setContent] =
        useState("");

    useEffect(() => {

        loadPost();

    }, [id]);

    const loadPost = async () => {

        const post =
            await getPost(id);

        setTitle(post.title);

        setContent(post.content);
    };

    const handleSubmit = async (e) => {

        e.preventDefault();

        await updatePost(id, {
            title,
            content
        });

        navigate(`/posts/${id}`);
    };

    return (
        <form onSubmit={handleSubmit}>

            <h1>게시글 수정</h1>

            <input
                value={title}
                onChange={(e) =>
                    setTitle(e.target.value)
                }
            />

            <textarea
                value={content}
                onChange={(e) =>
                    setContent(e.target.value)
                }
            />

            <button type="submit">
                수정
            </button>

        </form>
    );
}

export default PostUpdate;
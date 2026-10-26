import api from "../services/api.js";

const API = "/api/posts";

/**
 * 게시글 목록 조회
 *
 * page: 0부터 시작하는 페이지 번호
 * sort: 서버의 PostSort enum 값
 *       LATEST / OLDEST / TITLE / POPULAR
 * title: 제목 검색어. 빈 문자열이면 검색 조건 없이 전체 목록 조회
 *
 * 기존 getPosts(page, sort) 호출도 사용할 수 있도록
 * 세 번째 인자인 title의 기본값을 빈 문자열로 지정한다.
 */
export const getPosts = async (page = 0, sort = "LATEST",title="") => {
    const response = await api.get(API, {
        params: {
            page,
            size: 10,
            sort,
            // 검색어가 없으면 title 파라미터를 전송하지 않는다.
            // 검색어가 있으면 서버의 searchRequestDto.title로 전달된다.
            ...(title.trim() ? { title: title.trim() } : {})
        }
    });

    //api.js가 Axios response.data를 반환한다고 가정.
    //      *
    //      * response = {
    //      *   success: true,
    //      *   data: {
    //      *     content: [...],
    //      *     totalPages: 5,
    //      *     totalElements: 50,
    //      *     number: 0
    //      *   }
    //      * }
    //response.data는 Spring의 Page 객체다.
    return response.data;
};

export const getPost = async (postId,config={}) => {
    const response = await api.get(`/api/posts/${postId}`,config);

    return response.data;
};

export const createPost = async (data) => {
    return await api.post(API, data);
};

export const updatePost = async (postId,data) => {
    await api.patch(`/api/posts/${postId}/edit`,data);
};

export const deletePost = async (postId) => {
    await api.delete(`/api/posts/${postId}`);
};


////좋아요
export const likePost= async(postId)=>{
    await api.post(`/api/posts/${postId}/likes`);

}
export const unlikePost= async(postId)=>{

    await api.delete(`/api/posts/${postId}/unlikes`);

}

////댓글

export const getComment= async (postId,page = 0, sort = "LATEST") => {
    const response = await api.get(`/api/posts/${postId}/comments`, {
        params: {
            page,
            size: 10,
            sort
        }
    });

    return response.data;
}

export const createComment= async(postId,data)=>{

    /*
     * postId: 댓글이 속할 게시글 ID
     * data: 댓글 작성 데이터
     *
     * 예:
     * postId = 15
     * data = { content: "안녕하세요" }
     */

    if (postId == null || postId === "") {
        throw new Error("게시글 ID가 없습니다.");
    }

    // 서버로 댓글 생성 요청
    return await api.post(
        `/api/posts/${postId}/comments`,
        data
    );

}

export const deleteComment= async(postId,commentId)=>{

    await api.delete(`/api/posts/${postId}/comments/${commentId}`);

}




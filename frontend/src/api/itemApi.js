import api from '../services/api';

/**
 * 공통 api 인스턴스는 응답 인터셉터에서 이미 response.data를 반환한다.
 * 상품 Controller는 Page/상품 객체를 직접 반환하므로 여기서 .data를 다시 꺼내지 않는다.
 * signal은 화면 이동/검색 변경 때 더 이상 필요 없는 요청을 취소하는 데 사용한다.
 */
export const getItems = async (
    {
        page = 0,
        name = '',
        sortBy = 'LATEST',
        priceGoe = '',
        priceLoe = '',
        categoryId,

    } = {},
    signal
) => {
    const params = {
        page,
        size: 12,
        sort: sortBy,
    };

    if (name.trim()) params.name = name.trim();
    if (priceGoe !== '') params.priceGoe = priceGoe;
    if (priceLoe !== '') params.priceLoe = priceLoe;
    /*
     * 선택된 카테고리가 존재한다면
     * Axios 요청 쿼리 파라미터에 추가한다.
     *
     * 예: categoryId = "4"
     * 결과: /api/items?categoryId=4
     */
    if (categoryId !== '') {
        params.categoryId = categoryId;
    }

    // 서버 응답을 기다린 뒤 ApiResponse 안의 Page 객체를 반환한다.
    //api.js가 Axios의 data를 꺼냈으므로,
    // 여기서는 서버 ApiResponse 안의 data(Page 객체)를 꺼낸다
    const response = await api.get('/api/items', { params, signal });

    return response.data;
};

export const getItem = async (itemId, signal) => {
    const response = await api.get(
        `/api/items/${itemId}`,
        { signal }
    );

    // api.js: Axios 응답에서 HTTP 본문을 꺼냄
    // 여기: ApiResponse에서 실제 상품 객체를 꺼냄
    return response.data;
};
//api.js가 Axios의 data를 꺼냈으므로,
// 여기서는 서버 ApiResponse 안의 data(Page 객체)를 꺼낸다

export const createItem =async (formData)=>{
    const response =await api.post(
        `/api/items/new`,
        formData
    );
    return response.data;
}

export const updateItem =async (formData)=>{
    const response =await api.patch(
        `/api/items/{itemId}/update`,
        {formData}
    );
    return response.data;
}
export const deleteItem =async (formData)=>{
    const response =await api.delete(
        `/api/items/{itemId}`,
        {formData}
    );
    return response.data;
}


//상품 찜 등록
export const likeItem = async (itemId)=>{
    await api.post(`/api/items/${itemId}/likes`);
}

// 상품 찜 취소
export const unlikeItem = async (itemId) => {
    await api.delete(`/api/items/${itemId}/unlikes`);
};
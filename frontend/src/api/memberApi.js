import api from "@/services/api.js";

/*
 * 현재 로그인한 회원 정보 조회
 *
 * 서버에서 다음과 같은 ApiResponse를 반환한다고 가정한다.
 *
 * {
 *   success: true,
 *   data: {
 *     id: 1,
 *     loginId: "aaaa",
 *     nickname: "관리자",
 *     role: "ADMIN"
 *   }
 * }
 *
 * 주의:
 * api.js의 응답 인터셉터는
 * (response) => response.data 형태이므로
 * 이미 Axios 응답 객체의 data를 꺼낸 상태다.
 */

/**
 * 토큰을 전달하면 해당 토큰으로 조회한다.
 * 생략하면 공통 인터셉터가 메모리의 토큰을 사용한다.
 */
export const getMyInfo = async (accessToken) => {

    const config = accessToken
    ?{
        headers: {
            Authorization: `Bearer ${accessToken}`,
        },
        }
        : {};
    const response = await api.get("/api/auth/me", config);

    return response.data;
};
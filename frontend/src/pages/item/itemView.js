// 목록/상세에서 금액과 이미지 처리 규칙을 공유해 화면별 표시 차이를 방지한다.
const won = new Intl.NumberFormat('ko-KR');

export function formatPrice(value) {
    return typeof value === 'number' && Number.isFinite(value) && value >= 0
        ? `${won.format(value)}원`
        : '가격 확인 중';
}

export function getItemImages(item) {
    // 현재 API: imageUrls가 문자열 하나
    // 여러 이미지 API: imageUrls가 배열
    const images = Array.isArray(item?.imageUrls)
        ? item.imageUrls
        : typeof item?.imageUrls === 'string'
            ? [item.imageUrls]
            : [];

    // 메인에서 사용하는 thumbnailUrl도 함께 지원한다.
    const urls = [
        item?.thumbnailUrl,
        ...images
    ];

    // 빈 주소를 제외하고 공백과 중복을 제거한다.
    return [
        ...new Set(
            urls
                .filter((url) => typeof url === 'string' && url.trim())
                .map((url) => url.trim())
        )
    ];
}

export function getItemError(error, detail = false) {
    const status = error.response?.status;
    if (detail && (status === 404 || status === 400)) return '상품을 찾을 수 없습니다. 삭제되었거나 잘못된 주소일 수 있습니다.';
    if (status === 401 || status === 403) return '상품을 조회할 권한이 없습니다. 로그인 상태를 확인해 주세요.';
    return detail
        ? '상품 정보를 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.'
        : '상품 목록을 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.';
}

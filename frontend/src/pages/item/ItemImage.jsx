import { useState } from 'react';
import api from '../../services/api';

/**
 * 메인 화면과 같은 이미지 준비 중 상태를 사용한다.
 * 이미지 상대 경로는 프론트 주소가 아닌 기존 API 서버 주소 기준으로 해석한다.
 * 실패한 URL 자체를 기억하므로 다른 이미지로 바뀌면 새 URL을 정상적으로 시도한다.
 */
export default function ItemImage({ url, name, eager = false }) {
    const [failedUrl, setFailedUrl] = useState(null);
    let source = '';
    if (typeof url === 'string' && url.trim()) {
        try {
            const base = new URL(api.defaults.baseURL || '/', window.location.origin);
            const resolved = new URL(url, base);
            if (resolved.protocol === 'http:' || resolved.protocol === 'https:') source = resolved.href;
        } catch {
            // 잘못된 이미지 주소도 페이지 전체의 렌더링 오류로 이어지지 않게 한다.
            source = '';
        }
    }

    if (!source || failedUrl === source) {
        return (
            <div className="item-image-empty" role="img" aria-label={`${name} 이미지 준비 중`}>
                <svg viewBox="0 0 64 64" fill="none" aria-hidden="true">
                    <rect x="10" y="10" width="44" height="44" rx="12" stroke="currentColor" strokeWidth="1.5" />
                    <circle cx="24" cy="24" r="4" stroke="currentColor" strokeWidth="1.5" />
                    <path d="m13 45 13-13 10 10 7-7 10 10" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" />
                </svg>
                <span>이미지 준비 중</span>
            </div>
        );
    }

    return <img src={source} alt={name} loading={eager ? 'eager' : 'lazy'} decoding="async" onError={() => setFailedUrl(source)} />;
}

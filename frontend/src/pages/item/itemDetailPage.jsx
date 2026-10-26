import { useEffect, useState,useRef } from 'react';
import { Link, useLocation, useParams } from 'react-router-dom';
import { getItem,likeItem,unlikeItem } from '@/api/itemApi';
import ItemImage from './ItemImage';
import { formatPrice, getItemError, getItemImages } from './itemView';
import './ItemPages.css';

function ItemContent({ item, listUrl }) {
    const images = getItemImages(item);
    const [selectedImage, setSelectedImage] = useState(0);
    // 상세 API에서 받은 현재 사용자의 찜 여부와 전체 찜 개수로 초기화한다.
    // 기존 key={item.id} 덕분에 다른 상품으로 이동하면 상태도 새로 생성된다.
    const [wish, setWish] = useState(() => ({
        liked: Boolean(item.liked),
        likeCount: item.likeCount ?? 0,
    }));
    const [wishPending, setWishPending] = useState(false);
    const [wishError, setWishError] = useState('');

// 버튼 비활성화가 화면에 반영되기 전의 연속 클릭까지 막는다.
    const wishLock = useRef(false);

    const handleWish = async () => {
        if (wishLock.current) return;

        wishLock.current = true;
        setWishPending(true);
        setWishError('');

        const wasLiked = wish.liked;

        try {
            // 현재 찜 상태에 따라 등록 또는 취소 API를 호출한다.
            if (wasLiked) {
                await unlikeItem(item.id);
            } else {
                await likeItem(item.id);
            }

            // 서버 요청이 성공했을 때만 화면을 변경한다.
            // 실패하면 기존 하트와 개수가 그대로 유지된다.
            setWish((previous) => ({
                liked: !wasLiked,
                likeCount: Math.max(
                    0,
                    previous.likeCount + (wasLiked ? -1 : 1),
                ),
            }));
        } catch (error) {
            const status = error.response?.status;

            setWishError(
                status === 401 || status === 403
                    ? '찜 기능은 로그인 후 이용해 주세요.'
                    : error.response?.data?.message
                    || '찜을 처리하지 못했습니다. 다시 시도해 주세요.',
            );
        } finally {
            wishLock.current = false;
            setWishPending(false);
        }
    };
    const name = item.name || '이름 없는 상품';
    const description = typeof item.description === 'string' ? item.description.trim() : '';

    return (
        <>
            <article className="item-detail-card" aria-labelledby="item-detail-heading">
                <div className="item-gallery">
                    <div className="item-detail-image"><ItemImage url={images[selectedImage]} name={name} eager /></div>
                    {images.length > 1 && <div className="item-thumbnails" aria-label="상품 이미지 선택">{images.map((url, index) => <button key={url} type="button" aria-label={`${index + 1}번째 상품 이미지`} aria-pressed={selectedImage === index} onClick={() => setSelectedImage(index)}><ItemImage url={url} name={`${name} ${index + 1}`} /></button>)}</div>}
                </div>
                <div className="item-detail-summary">
                    <p className="item-eyebrow">KIRBY WORLD · SHOP</p>
                    <h1 id="item-detail-heading">{name}</h1>
                    <p className="item-detail-price">{formatPrice(item.price)}</p>
                    <dl className="item-detail-facts"><div><dt>상품 번호</dt><dd>{item.id}</dd></div><div><dt>상품명</dt><dd>{name}</dd></div><div><dt>판매 가격</dt><dd>{formatPrice(item.price)}</dd></div></dl>
                    {/* 현재 상세 API는 id/name/price만 제공한다.
                        재고/배송/할인/옵션을 임의로 만들어 구매 가능 상태처럼 표시하지 않는다.
                        추후 응답에 stockQuantity가 추가되면 실제 값에 한해 표시한다. */}
                    {Number.isInteger(item.stockQuantity) && item.stockQuantity >= 0 && <p className="item-stock-note">{item.stockQuantity === 0 ? '현재 품절된 상품입니다.' : `남은 수량 ${item.stockQuantity.toLocaleString('ko-KR')}개`}</p>}
                    <div className="item-detail-action">
                        <button
                            type="button"
                            className="item-wish-button"
                            onClick={handleWish}
                            disabled={wishPending}
                            aria-pressed={wish.liked}
                            aria-busy={wishPending}
                        >
        <span aria-hidden="true">
            {wish.liked ? '♥' : '♡'}
        </span>

                            <span>
            {wishPending
                ? '처리 중...'
                : wish.liked
                    ? '찜 취소'
                    : '찜하기'}
        </span>

                            <strong>{wish.likeCount.toLocaleString('ko-KR')}</strong>
                        </button>

                        {wishError && (
                            <p className="item-wish-error" role="alert">
                                {wishError}
                            </p>
                        )}

                        <Link
                            className="item-button item-button-primary"
                            to={listUrl}
                        >
                            상품 목록으로 돌아가기
                            <span aria-hidden="true">→</span>
                        </Link>

                        <p>다른 상품도 함께 둘러보세요.</p>
                    </div>
                </div>
            </article>
            <section className="item-detail-description" aria-labelledby="item-description-heading"><div className="item-section-heading"><span className="item-eyebrow">DETAILS</span><h2 id="item-description-heading">상품 상세정보</h2></div>{description ? <p className="item-description-copy">{description}</p> : <p className="item-description-empty">아직 등록된 상세 설명이 없습니다.</p>}</section>
        </>
    );
}

export default function ItemDetailPage() {
    const { itemId } = useParams();
    const location = useLocation();
    const [retryKey, setRetryKey] = useState(0);
    const [result, setResult] = useState({ key: null, status: 'loading' });
    const validId = /^[1-9]\d*$/.test(itemId || '');
    const requestKey = `${itemId}:${retryKey}`;
    // 복귀 주소는 상품 목록의 내부 경로만 허용한다. 직접 접속한 경우 /items로 이동한다.
    const previousList = location.state?.itemListUrl;
    const listUrl = typeof previousList === 'string' && /^\/items(?:\?[^#]*)?$/.test(previousList) ? previousList : '/items';

    useEffect(() => {
        if (!validId) return;
        const controller = new AbortController();
        async function loadItem() {
            try {
                const item = await getItem(itemId, controller.signal);
                if (!item || item.id == null || typeof item.name !== 'string') throw new Error('상품 상세 응답 형식이 올바르지 않습니다.');
                if (!controller.signal.aborted) setResult({ key: requestKey, status: 'success', data: item });
            } catch (error) {
                if (!controller.signal.aborted) setResult({ key: requestKey, status: 'error', message: getItemError(error, true) });
            }
        }
        void loadItem();
        return () => controller.abort();
    }, [itemId, requestKey, validId]);

    const loading = validId && result.key !== requestKey;
    const item = !loading && validId && result.status === 'success' ? result.data : null;
    const error = !validId ? '올바른 상품 주소가 아닙니다. 상품 목록에서 다시 선택해 주세요.' : !loading && result.status === 'error' ? result.message : '';

    return (
        <section className="item-page item-detail-page" aria-label="상품 상세" aria-busy={loading}>
            <div className="item-container">
                <nav className="item-breadcrumb" aria-label="현재 위치"><Link to="/main">메인</Link><span aria-hidden="true">/</span><Link to={listUrl}>상품 목록</Link><span aria-hidden="true">/</span><span aria-current="page">상품 상세</span></nav>
                <Link to={listUrl} className="item-back-link"><span aria-hidden="true">←</span> 목록으로</Link>
                {loading ? <div className="item-feedback" role="status"><span className="item-loading-dot" aria-hidden="true" /><h1>상품 정보를 불러오고 있어요.</h1><p>잠시만 기다려 주세요.</p></div>
                    : error ? <div className="item-feedback" role="alert"><h1>상품을 확인할 수 없어요.</h1><p>{error}</p><div className="item-feedback-actions">{validId && <button type="button" className="item-button item-button-primary" onClick={() => setRetryKey((previous) => previous + 1)}>다시 시도</button>}<Link to={listUrl} className="item-button item-button-outline">상품 목록</Link></div></div>
                        : item && <ItemContent key={item.id} item={item} listUrl={listUrl} />}
            </div>
        </section>
    );
}

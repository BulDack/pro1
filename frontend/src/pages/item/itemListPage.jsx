import { useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { getItems } from '@/api/itemApi';
import ItemImage from './ItemImage';
import { formatPrice, getItemError, getItemImages } from './itemView';
import './ItemPages.css';
import './ItemCategorySearch.css';
import CategoryFilter from './CategoryFilter';

const SORTS = ['LATEST', 'PRICE_ASC', 'PRICE_DESC'];
const numberFormat = new Intl.NumberFormat('ko-KR');
const MAX_PRICE = 2147483647; // 백엔드 Integer 검색 조건의 범위에 맞춘다.


//검색
function SearchForm({ name, priceGoe, priceLoe,categoryId, onSearch }) {
    const [draft, setDraft] = useState({ name, priceGoe, priceLoe,categoryId, });
    const [error, setError] = useState('');

    function submit(event) {
        event.preventDefault();
        const prices = [draft.priceGoe, draft.priceLoe];
        if (prices.some((value) => value !== '' && (!/^\d+$/.test(value) || Number(value) > MAX_PRICE))) {
            setError('가격은 0원 이상 2,147,483,647원 이하의 정수로 입력해 주세요.');
            return;
        }
        if (prices.every((value) => value !== '') && Number(prices[0]) > Number(prices[1])) {
            setError('최소 가격은 최대 가격보다 클 수 없습니다.');
            return;
        }
        setError('');
        onSearch({ ...draft, name: draft.name.trim() });
    }

    return (
        <form className="item-search" onSubmit={submit} aria-label="상품 검색">
            <div className="item-search-fields">
                <CategoryFilter
                    value={draft.categoryId}
                    onChange={categoryId =>
                        setDraft(previous => ({
                            ...previous,
                            categoryId,
                        }))
                    }
                />
                <label className="item-search-name">
                    <span>상품명</span>
                    <input type="search" name="name" maxLength={200} placeholder="찾으시는 상품명을 입력해 주세요" value={draft.name}
                           onChange={(event) => setDraft({ ...draft, name: event.target.value })} />
                </label>
                <fieldset className="item-price-range">
                    <legend>가격 범위</legend>
                    <div>
                        <input aria-label="최소 가격" name="priceGoe" type="number" min="0" max={MAX_PRICE} step="1" placeholder="최소 가격" value={draft.priceGoe}
                               onChange={(event) => setDraft({ ...draft, priceGoe: event.target.value })} />
                        <span aria-hidden="true">–</span>
                        <input aria-label="최대 가격" name="priceLoe" type="number" min="0" max={MAX_PRICE} step="1" placeholder="최대 가격" value={draft.priceLoe}
                               onChange={(event) => setDraft({ ...draft, priceLoe: event.target.value })} />
                    </div>
                </fieldset>
                <button className="item-button item-button-primary item-search-submit" type="submit">
                    <svg viewBox="0 0 20 20" fill="none" aria-hidden="true"><circle cx="8.5" cy="8.5" r="5.5" stroke="currentColor" strokeWidth="1.7" /><path d="m13 13 4 4" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" /></svg>
                    검색
                </button>
            </div>
            {error && <p className="item-form-error" role="alert">{error}</p>}
        </form>
    );
}

function ItemCard({ item, listUrl }) {
    const name = item.name || '이름 없는 상품';
    return (
        <li className="item-card">
            {/* 카드 전체를 하나의 링크로 만들어 이미지/이름 어디를 눌러도 상세로 이동한다.
                복귀 주소를 전달해 상세에서 돌아왔을 때 검색과 페이지를 유지한다. */}
            <Link to={`/items/${item.id}`} state={{ itemListUrl: listUrl }} className="item-card-link">
                <div className="item-card-image"><ItemImage url={getItemImages(item)[0]} name={name} /></div>
                <div className="item-card-copy">
                    <span className="item-card-label">KIRBY WORLD</span>
                    <h2>{name}</h2>
                    <div className="item-card-bottom"><p>{formatPrice(item.price)}</p><span className="item-card-arrow" aria-hidden="true">↗</span></div>
                </div>
            </Link>
        </li>
    );
}

export default function ItemListPage() {
    const [searchParams, setSearchParams] = useSearchParams();
    const [retryKey, setRetryKey] = useState(0);
    const [result, setResult] = useState({ key: null, status: 'loading' });
    // URL을 조회 조건의 기준으로 사용한다. 새로고침과 브라우저 뒤로 가기에서도 조건이 유지된다.
    const rawPage = searchParams.get('page') || '0';
    const page = /^\d+$/.test(rawPage) && Number(rawPage) <= MAX_PRICE ? Number(rawPage) : 0;
    const name = searchParams.get('name') || '';
    const rawSort = searchParams.get('sortBy');
    const sortBy = SORTS.includes(rawSort) ? rawSort : 'LATEST';
    const priceGoe = searchParams.get('priceGoe') || '';
    const priceLoe = searchParams.get('priceLoe') || '';
    const categoryId = searchParams.get('categoryId') || '';
    const requestKey = JSON.stringify([page, name, sortBy, priceGoe, priceLoe, categoryId, retryKey]); //카테고리가 변경되어도 새로운검색으로 인식
    const listUrl = `/items${searchParams.size ? `?${searchParams}` : ''}`;
    const hasFilters = Boolean(name || priceGoe || priceLoe || categoryId); //검색 초기화

    useEffect(() => {
        const controller = new AbortController();
        async function loadItems() {
            try {
                const response = await getItems({ page, name, sortBy, priceGoe, priceLoe,categoryId }, controller.signal);
                if (!Array.isArray(response?.content) || !Number.isInteger(response.totalPages) || !Number.isInteger(response.totalElements)) {
                    throw new Error('상품 목록 응답 형식이 올바르지 않습니다.');
                }
                // 요청 취소와 별도로 확인해 늦은 응답이 새 검색 결과를 덮어쓰지 않도록 한다.
                if (!controller.signal.aborted) setResult({ key: requestKey, status: 'success', data: response });
            } catch (error) {
                if (!controller.signal.aborted) setResult({ key: requestKey, status: 'error', message: getItemError(error) });
            }
        }
        void loadItems();
        return () => controller.abort();
    }, [requestKey]);

    // 새 조건의 응답이 오기 전에는 이전 목록을 표시하지 않는다.
    const loading = result.key !== requestKey;
    const data = !loading && result.status === 'success' ? result.data : null;
    const error = !loading && result.status === 'error' ? result.message : '';
    const items = data?.content || [];
    const totalPages = data?.totalPages || 0;
    const pageStart = Math.max(0, Math.min(page - 2, totalPages - 5));
    const pages = Array.from({ length: Math.min(5, totalPages) }, (_, index) => pageStart + index);

    function updateParams(values) {
        const next = new URLSearchParams(searchParams);
        Object.entries(values).forEach(([key, value]) => {
            if (value === '' || (key === 'page' && value === 0)) next.delete(key);
            else next.set(key, String(value));
        });
        setSearchParams(next);
    }

    function reset() {
        setSearchParams({});
        // 동일한 조건에서의 초기화도 재시도할 수 있게 요청 키를 갱신한다.
        setRetryKey((previous) => previous + 1);
    }

    return (
        <section className="item-page" aria-labelledby="item-list-heading">
            <div className="item-container">
                <nav className="item-breadcrumb" aria-label="현재 위치"><Link to="/main">메인</Link><span aria-hidden="true">/</span><span aria-current="page">상품 목록</span></nav>
                <header className="item-page-heading">
                    <div><p className="item-eyebrow">KIRBY WORLD · SHOP</p><h1 id="item-list-heading">취향을 발견하는 시간.</h1><p className="item-page-description">마음에 드는 상품을 찾아 천천히 둘러보세요.</p></div>
                    <div
                        style={{
                            display: 'flex',
                            flexWrap: 'wrap',
                            alignItems: 'center',
                            gap: '12px',
                        }}
                    >
    <span className="item-heading-tag">
        작은 발견, 새로운 즐거움
    </span>

                        {/* 기존 버튼 스타일을 사용해 상품등록 페이지로 이동한다. */}
                        <Link
                            to="/items/new"
                            className="item-button item-button-primary"
                        >
                            <svg
                                width="18"
                                height="18"
                                viewBox="0 0 24 24"
                                fill="none"
                                stroke="currentColor"
                                strokeWidth="1.8"
                                strokeLinecap="round"
                                aria-hidden="true"
                            >
                                <path d="M12 5v14M5 12h14" />
                            </svg>
                            상품 등록
                        </Link>
                    </div>
                </header>

                <div className="item-search-panel">
                    {/* URL이 변경되면 폼도 해당 조건으로 다시 생성한다.
                        입력 중인 값과 제출한 값을 분리해 타이핑마다 요청하지 않는다. */}
                    <SearchForm key={JSON.stringify([name, priceGoe, priceLoe,categoryId, retryKey])} name={name} priceGoe={priceGoe} categoryId={categoryId} priceLoe={priceLoe}
                                onSearch={(values) => { updateParams({ ...values, page: 0 });
                                    /*
                                    * 검색 버튼을 누르면 URL의 검색 조건을 갱신한다.
                                    *
                                     * URL이 바뀌면 categoryId가 변경되고,
                                     * requestKey도 변경된다.
                                     *
                                     * 따라서 useEffect가 자동으로 다시 실행되므로
                                     * setRetryKey()를 별도로 호출할 필요가 없다.
                                     *///setRetryKey((previous) => previous + 1);
                                }} />
                </div>

                <div className="item-list-toolbar">
                    <p role="status">{data ? <>총 <strong>{numberFormat.format(data.totalElements)}</strong>개의 상품</> : '상품 목록'}{name && <span className="item-applied-keyword"> · “{name}”</span>}</p>
                    <div className="item-list-tools">
                        {hasFilters && <button type="button" className="item-text-button" onClick={reset}>검색 초기화</button>}
                        <label className="item-sort"><span className="item-sr-only">상품 정렬</span><select value={sortBy} onChange={(event) => updateParams({ sortBy: event.target.value, page: 0 })}><option value="LATEST">최신순</option><option value="PRICE_ASC">낮은 가격순</option><option value="PRICE_DESC">높은 가격순</option></select></label>
                    </div>
                </div>

                <div aria-busy={loading}>
                    {loading ? <div className="item-feedback" role="status"><span className="item-loading-dot" aria-hidden="true" /><h2>상품을 불러오고 있어요.</h2><p>잠시만 기다려 주세요.</p></div>
                        : error ? <div className="item-feedback" role="alert"><h2>잠시 연결이 어려워요.</h2><p>{error}</p><button type="button" className="item-button item-button-primary" onClick={() => setRetryKey((previous) => previous + 1)}>다시 시도</button></div>
                            : items.length === 0 ? <div className="item-feedback"><span className="item-empty-mark" aria-hidden="true">⌕</span><h2>{hasFilters ? '찾으시는 상품이 없어요.' : page > 0 ? '이 페이지에는 상품이 없어요.' : '새로운 상품을 기다리고 있어요.'}</h2><p>{hasFilters ? '다른 상품명이나 가격 범위로 다시 찾아보세요.' : '상품이 등록되면 이곳에서 만나보실 수 있습니다.'}</p>{(hasFilters || page > 0) && <button type="button" className="item-button item-button-outline" onClick={reset}>전체 상품 보기</button>}</div>
                                : <ul className="item-grid">{items.map((item) => <ItemCard key={item.id} item={item} listUrl={listUrl} />)}</ul>}
                </div>

                {data && items.length > 0 && totalPages > 1 && <nav className="item-pagination" aria-label="상품 목록 페이지">
                    <button type="button" disabled={page === 0} onClick={() => updateParams({ page: page - 1 })} aria-label="이전 페이지">‹</button>
                    {pages.map((number) => <button type="button" key={number} aria-label={`${number + 1}페이지`} aria-current={page === number ? 'page' : undefined} onClick={() => updateParams({ page: number })}>{number + 1}</button>)}
                    <button type="button" disabled={page >= totalPages - 1} onClick={() => updateParams({ page: page + 1 })} aria-label="다음 페이지">›</button>
                </nav>}
            </div>
        </section>
    );
}

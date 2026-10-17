import { useEffect, useState } from 'react';
import { getHome } from '@/api/homeApi';
import {Link} from "react-router-dom";

import './MainPage.css';

const numberFormat = new Intl.NumberFormat('ko-KR');

function PostSection({ id, title, description, posts }) {
    return (
        <section className="main-section" aria-labelledby={id}>
            <div className="main-section-heading">
                <h2 id={id}>{title}</h2>
                <p>{description}</p>
            </div>
            {posts.length === 0 ? (
                <p className="main-empty">아직 등록된 게시글이 없습니다.</p>
            ) : (
                <ol className="main-post-list">
                    {posts.map((post, index) => (
                        <li key={post.postId} className="main-post-row">
                            <span className="main-post-number">{String(index + 1).padStart(2, '0')}</span>
                            <div className="main-post-content">
                                <h3>{post.title}</h3>
                                <p>{post.nickname || '작성자 미상'}</p>
                            </div>
                            <div className="main-post-stats">
                                <span>조회 {numberFormat.format(post.viewCount ?? 0)}</span>
                                <span>좋아요 {numberFormat.format(post.likeCount ?? 0)}</span>
                            </div>
                        </li>
                    ))}
                </ol>
            )}
        </section>
    );
}

function ProductImage({ url, name }) {
    const [failed, setFailed] = useState(false);
    if (!url || failed) {
        return <div className="main-image-placeholder" role="img" aria-label={`${name} 이미지 준비 중`}>이미지 준비 중</div>;
    }
    // 서버가 반환하는 /uploads/... 상대 경로도 백엔드 주소 기준으로 표시합니다.
    const imageUrl = new URL(url, 'http://localhost:8080/uploads').href;
    return <img src={imageUrl} alt={name} loading="lazy" onError={() => setFailed(true)} />;
}

function ItemSection({ id, title, description, items, badge }) {
    return (
        <section className="main-section" aria-labelledby={id}>
            <div className="main-section-heading">
                <h2 id={id}>{title}</h2>
                <p>{description}</p>
            </div>
            {items.length === 0 ? (
                <p className="main-empty">아직 표시할 상품이 없습니다.</p>
            ) : (
                <ul className="main-item-grid">
                    {items.map((item) => (
                        <li key={item.id} className="main-item-card">
                            <div className="main-item-image">
                                <ProductImage key={item.thumbnailUrl} url={item.thumbnailUrl} name={item.name} />
                                <span className="main-item-badge">{badge}</span>
                            </div>
                            <h3>{item.name}</h3>
                            <p className="main-item-price">{numberFormat.format(item.price)}<span>원</span></p>
                        </li>
                    ))}
                </ul>
            )}
        </section>
    );
}

export default function MainPage() {
    const [data, setData] = useState(null);
    const [error, setError] = useState('');
    const [isLoading, setIsLoading] = useState(true);
    const [requestKey, setRequestKey] = useState(0);

    useEffect(() => {
        const controller = new AbortController();
        async function loadHome() {
            try {
                const response = await getHome(controller.signal);
                if (!controller.signal.aborted) setData(response);
            } catch (requestError) {
                if (!controller.signal.aborted) {
                    setError(requestError.response?.data?.message || '목록을 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.');
                }
            } finally {
                if (!controller.signal.aborted) setIsLoading(false);
            }
        }
        loadHome();
        return () => controller.abort();//
    }, [requestKey]);

    const retry = () => {
        setError('');
        setIsLoading(true);
        setRequestKey((previous) => previous + 1);
    };

    return (
        <div className="main-page">
            <div className="main-page-container">
                <header className="main-page-intro">
                    <p className="main-page-label">KIRBY WORLD</p>
                    <h1>오늘의 이야기를 만나보세요.</h1>
                    <p>새로운 이야기부터 인기 상품까지, 한곳에서 둘러보세요.</p>
                    <nav
                        aria-label="목록 바로가기"
                        style={{ display: 'flex', gap: '16px', marginTop: '16px' }}
                    >
                        <Link to="/posts">게시글 목록</Link>
                        <Link to="/items">상품 목록</Link>
                    </nav>
                </header>
                {isLoading ? (
                    <div className="main-feedback" role="status">게시글과 상품을 불러오는 중입니다...</div>
                ) : error ? (
                    <div className="main-feedback" role="alert">
                        <p>{error}</p>
                        <button type="button" onClick={retry}>다시 시도</button>
                    </div>
                ) : (
                    <>
                        <div className="main-post-sections">
                            <PostSection id="recent-posts" title="최근 게시글" description="방금 올라온 새로운 이야기" posts={data?.recentPosts ?? []} />
                            <PostSection id="popular-posts" title="인기 게시글" description="조회수가 높은 이야기" posts={data?.popularPosts ?? []} />
                        </div>
                        <ItemSection id="popular-items" title="인기 상품" description="최근 7일간 많이 판매된 상품" items={data?.popularItem ?? []} badge="BEST" />
                        <ItemSection id="new-items" title="신상품" description="새롭게 찾아온 상품" items={data?.newItem ?? []} badge="NEW" />
                    </>
                )}
            </div>
        </div>
    );
}

import { useId, useState } from 'react';
import './ItemCategorySearch.css';

const CATEGORY_TREE = [
    {
        id: '1',
        name: '전자기기',
        children: [
            {
                id: '2',
                name: '컴퓨터',
                children: [
                    { id: '4', name: '노트북' },
                    { id: '5', name: '데스크톱' },
                ],
            },
            {
                id: '3',
                name: '모바일',
                children: [
                    { id: '6', name: '스마트폰' },
                    { id: '7', name: '태블릿' },
                ],
            },
        ],
    },
    {
        id: '8',
        name: '옷',
        children: [
            {
                id: '9',
                name: '상의',
                children: [
                    { id: '11', name: '티셔츠' },
                    { id: '12', name: '셔츠' },
                ],
            },
            {
                id: '10',
                name: '하의',
                children: [
                    { id: '13', name: '청바지' },
                    { id: '14', name: '슬랙스' },
                ],
            },
        ],
    },
    {
        id: '15',
        name: '음식',
        children: [
            {
                id: '16',
                name: '신선식품',
                children: [
                    { id: '18', name: '중식' },
                    { id: '19', name: '한식' },
                ],
            },
            {
                id: '17',
                name: '가공식품',
                children: [
                    { id: '20', name: '일식' },
                    { id: '21', name: '양식' },
                ],
            },
        ],
    },
];

/**
 * 부모가 categoryId 문자열 하나를 소유하는 제어 컴포넌트이다.
 * 사용 예: <CategoryFilter value={draft.categoryId} onChange={...} />
 * 선택 시 onChange('4'), 전체 선택 시 onChange('')를 호출한다.
 * 이 컴포넌트는 검색 요청을 보내지 않는다. 부모의 검색 버튼이 검색을 실행한다.
 */
export default function CategoryFilter({ value = '', onChange }) {
    // 접기 상태만 내부에서 관리한다. 접어도 부모의 선택 ID는 그대로 유지된다.
    const [isOpen, setIsOpen] = useState(false);
    // 한 페이지에 여러 필터가 있어도 접근성 연결 ID가 겹치지 않게 한다.
    const id = useId();
    const triggerId = `${id}-trigger`;
    const panelId = `${id}-panel`;
    const guideId = `${id}-guide`;

    // 마지막 선택 ID 하나에서 상위 경로를 역으로 찾는다.
    // 예: '4' → 전자기기 / 컴퓨터 / 노트북. 별도의 중·소분류 상태를 두지 않아
    // 부모에서 ID를 초기화하거나 변경해도 화면의 선택과 경로가 함께 갱신된다.
    const root = CATEGORY_TREE.find(category =>
            category.id === value || category.children.some(middle =>
                middle.id === value || middle.children.some(leaf => leaf.id === value)
            )
    );
    const middle = root?.children.find(category =>
        category.id === value || category.children.some(leaf => leaf.id === value)
    );
    const leaf = middle?.children.find(category => category.id === value);
    const selectedPath = [root, middle, leaf].filter(Boolean);
    const selectedName = leaf?.name || middle?.name || root?.name || '전체 상품';
    const pathText = selectedPath.map(category => category.name).join(' › ') || '전체 상품';

    return (
        <fieldset className="item-category-filter">
            {/* legend는 fieldset의 첫 자식으로 둬 입력 그룹 이름을 올바르게 제공한다. */}
            <legend className="item-category-sr-only">상품 카테고리</legend>

            <h3 className="item-category-heading">
                {/* 헤더 전체가 실제 button이다. Tab으로 이동하고 Enter/Space로 토글한다.
                    type="button"으로 검색 폼이 의도치 않게 제출되는 것을 막는다. */}
                <button
                    id={triggerId}
                    type="button"
                    className="item-category-toggle"
                    aria-expanded={isOpen}
                    aria-controls={panelId}
                    onClick={() => setIsOpen(previous => !previous)}
                >
                    <span className="item-category-icon" aria-hidden="true">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.6">
                            <rect x="3" y="3" width="7" height="7" rx="2" />
                            <rect x="14" y="3" width="7" height="7" rx="2" />
                            <rect x="3" y="14" width="7" height="7" rx="2" />
                            <rect x="14" y="14" width="7" height="7" rx="2" />
                        </svg>
                    </span>
                    <span className="item-category-heading-copy">
                        <span className="item-category-title">카테고리</span>
                        <span className="item-category-subtitle">취향에 맞는 상품을 찾아보세요</span>
                    </span>
                    <span className="item-category-toggle-label">{isOpen ? '접기' : '펼치기'}</span>
                    <span className="item-category-chevron" aria-hidden="true">
                        <svg viewBox="0 0 20 20" fill="none" stroke="currentColor" strokeWidth="1.8">
                            <path d="m5 7.5 5 5 5-5" strokeLinecap="round" strokeLinejoin="round" />
                        </svg>
                    </span>
                </button>
            </h3>

            {/* 접혀 있어도 선택 칩·전체 경로를 확인하고 선택을 해제할 수 있다.
                해제 버튼은 토글 버튼 밖에 있어 해제할 때 아코디언이 열리지 않는다. */}
            <div className="item-category-summary">
                <div className="item-category-selection" role="status" aria-live="polite" aria-atomic="true">
                    <span className="item-category-chip">{selectedName}</span>
                    <span className="item-category-path">
                        <span className="item-category-sr-only">선택한 분류: </span>
                        {pathText}
                    </span>
                </div>
                {value && (
                    <button className="item-category-reset" type="button" onClick={() => onChange('')}>
                        <span aria-hidden="true">↺</span> 선택 해제
                    </button>
                )}
            </div>

            {/* hidden을 사용해 접힌 패널의 입력이 Tab 순서와 접근성 트리에서 빠지게 한다.
                패널 자체는 유지하므로 aria-controls가 항상 실제 요소를 가리킨다. */}
            <div id={panelId} className="item-category-content" hidden={!isOpen}
                 role="region" aria-labelledby={triggerId}>
                <p id={guideId} className="item-category-guide">
                    원하는 분류까지만 선택해도 하위 상품을 함께 검색해요.
                </p>

                <div className="item-category-step-label"><span aria-hidden="true">01</span> 대분류</div>
                <div className="item-category-root-buttons" role="group" aria-label="대분류" aria-describedby={guideId}>
                    <button type="button" aria-pressed={!value} onClick={() => onChange('')}>
                        전체 상품 <span className="item-category-check" aria-hidden="true">✓</span>
                    </button>
                    {CATEGORY_TREE.map(category => (
                        <button key={category.id} type="button" aria-pressed={root?.id === category.id}
                            // 상위 ID로 교체하면 이전 중·소분류 선택도 자동으로 해제된다.
                                onClick={() => onChange(category.id)}>
                            {category.name} <span className="item-category-check" aria-hidden="true">✓</span>
                        </button>
                    ))}
                </div>

                <div className="item-category-detail-fields">
                    <label htmlFor={`${id}-middle`}>
                        <span className="item-category-step-label"><span aria-hidden="true">02</span> 중분류</span>
                        <select id={`${id}-middle`} name="middleCategoryId" value={middle?.id || ''} disabled={!root}
                            // 중분류의 '전체'는 선택된 대분류 ID로 돌아간다.
                                onChange={event => onChange(event.target.value || root?.id || '')}>
                            <option value="">{root ? `${root.name} 전체` : '대분류를 먼저 선택해 주세요'}</option>
                            {root?.children.map(category => (
                                <option key={category.id} value={category.id}>{category.name}</option>
                            ))}
                        </select>
                    </label>
                    <label htmlFor={`${id}-leaf`}>
                        <span className="item-category-step-label"><span aria-hidden="true">03</span> 소분류</span>
                        <select id={`${id}-leaf`} name="leafCategoryId" value={leaf?.id || ''} disabled={!middle}
                            // 소분류의 '전체'는 선택된 중분류 ID로 돌아간다.
                                onChange={event => onChange(event.target.value || middle?.id || '')}>
                            <option value="">{middle ? `${middle.name} 전체` : '중분류를 먼저 선택해 주세요'}</option>
                            {middle?.children.map(category => (
                                <option key={category.id} value={category.id}>{category.name}</option>
                            ))}
                        </select>
                    </label>
                </div>
                <p className="item-category-footer">분류를 고른 뒤 검색 버튼을 눌러 주세요.</p>
            </div>
        </fieldset>
    );
}

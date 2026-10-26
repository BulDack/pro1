import { useRef, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { createItem } from '@/api/itemApi.js';
import CategoryFilter from './CategoryFilter';
import './ItemPages.css';
import './ItemCreatePage.css';

// Java의 Integer/int는 32비트 정수다. HTML의 min/max 속성뿐 아니라
// 제출 직전에도 범위를 확인해 소수·음수·빈 문자열이 DTO에 전달되지 않게 한다.
const MAX_INTEGER = 2147483647;
const IMAGE_TYPES = ['image/jpeg', 'image/png', 'image/webp'];

export default function ItemCreatePage() {
    /* 입력 중에는 숫자도 문자열로 보관한다.
     * 예: 가격 입력을 지울 때 ''를 유지해야 한다. Number('')는 0이므로
     * 입력 즉시 숫자로 바꾸면 '미입력'과 '0원'을 구별할 수 없다.
     * 실제 숫자 변환은 제출 직전 검증이 끝난 뒤 수행한다.
     */
    const [form, setForm] = useState({
        name: '', price: '', description: '', stockQuantity: '',
        categoryId: '', createAt: '',
    });
    const [images, setImages] = useState([]);
    const [errorMessage, setErrorMessage] = useState('');
    const [isSubmitting, setIsSubmitting] = useState(false);
    const navigate = useNavigate();

    // state는 다음 렌더링에서 반영된다. ref를 함께 사용하면 그 전에 발생하는
    // 빠른 연속 제출도 즉시 차단한다. 서버에 중복 등록 요청을 보내지 않기 위함이다.
    const submittingRef = useRef(false);

    function changeField(event) {
        const { name, value } = event.target;
        setForm(previous => ({ ...previous, [name]: value }));
    }

    function handleImageChange(event) {
        // FileList는 파일 입력의 전용 객체이므로 배열로 변환해 상태에 보관한다.
        // 새로 파일을 선택하면 이전 선택을 대체하며, 파일명만 표시한다.
        // 실제 파일 크기·내용·업로드 권한 검증은 서버에서도 수행해야 한다.
        setImages(Array.from(event.target.files || []));
        setErrorMessage('');
    }

    async function handleSubmit(event) {
        event.preventDefault(); // 브라우저의 기본 폼 전송과 전체 새로고침을 막는다.
        if (submittingRef.current) return;
        setErrorMessage('');

        const name = form.name.trim();
        const description = form.description.trim();
        // @NotBlank와 @Size(max=200)에 맞춰 공백만 있는 값과 긴 상품명을 막는다.
        if (!name || name.length > 200) {
            setErrorMessage('상품명은 공백을 제외하고 1~200자로 입력해 주세요.');
            return;
        }
        if (!description) {
            setErrorMessage('상품 설명을 입력해 주세요.');
            return;
        }
        for (const [label, value] of [['가격', form.price], ['재고 수량', form.stockQuantity]]) {
            // 정규식으로 정수 표기인지 먼저 확인한다. Number만 사용하면 빈 값이나
            // 지수 표기도 숫자로 바뀔 수 있다. 0은 @PositiveOrZero가 허용하므로 유효하다.
            if (!/^\d+$/.test(value) || !Number.isInteger(Number(value)) || Number(value) > MAX_INTEGER) {
                setErrorMessage(`${label}은 0~2,147,483,647 사이의 정수로 입력해 주세요.`);
                return;
            }
        }
        // CategoryFilter의 선택값은 문자열 하나다. '전체 상품'은 ''이므로 등록에
        // 사용할 수 없다. 선택된 대·중·소분류 ID 하나를 Long categoryId로 전달한다.
        // JavaScript에서 정확히 표현할 수 없는 큰 정수는 조용히 반올림하지 않고 막는다.
        if (!/^\d+$/.test(form.categoryId) || !Number.isSafeInteger(Number(form.categoryId)) || Number(form.categoryId) <= 0) {
            setErrorMessage('상품을 등록할 카테고리를 선택해 주세요.');
            return;
        }
        // 현재 Controller의 @RequestPart("images")는 필수 파트다.
        // 파일을 하나도 선택하지 않으면 해당 파트가 생기지 않으므로 제출 전에 확인한다.
        if (images.length === 0) {
            setErrorMessage('상품 이미지를 한 장 이상 선택해 주세요.');
            return;
        }
        // accept는 파일 선택창의 안내일 뿐 완전한 검증이 아니므로 MIME도 확인한다.
        if (images.some(file => !IMAGE_TYPES.includes(file.type) || file.size === 0)) {
            setErrorMessage('내용이 있는 JPG, PNG, WEBP 이미지 파일을 선택해 주세요.');
            return;
        }

        /* LocalDateTime에는 시간대 정보가 없으므로 toISOString()으로 UTC 변환하거나
         * Z를 붙이지 않는다. datetime-local의 '2026-10-26T15:30'은 초를 덧붙여
         * '2026-10-26T15:30:00'으로 보낸다. 선택하지 않으면 nullable 필드이므로 null.
         * 현재 ItemService는 request.createAt()을 사용하지 않아 저장 일시를 지정하지는 않는다.
         */
        const createAt = form.createAt
            ? (form.createAt.length === 16 ? `${form.createAt}:00` : form.createAt)
            : null;

        // DTO 필드명과 정확히 일치하는 JSON 객체다. 가격·재고·categoryId는 숫자다.
        const request = {
            name,
            price: Number(form.price),
            description,
            stockQuantity: Number(form.stockQuantity),
            // 실제 이미지 주소는 서버가 업로드 파일을 저장한 뒤 생성한다.
            // 로컬 파일명이나 브라우저 blob URL을 저장용 imageUrl로 보내면 안 된다.
            // 현재 서비스는 request.imageUrl()을 사용하지 않으므로 빈 목록을 보낸다.
            imageUrl: [],
            categoryId: Number(form.categoryId),
            createAt,
        };

        /* 하나의 HTTP 요청에 JSON과 파일을 함께 담는다.
         * request: application/json Blob → @RequestPart("request") ItemCreateRequest
         * images: 같은 이름으로 반복 추가한 File → @RequestPart("images") List<MultipartFile>
         * JSON.stringify는 위 request 객체에만 적용하고 FormData 자체에는 적용하지 않는다.
         */
        const formData = new FormData();
        formData.append('request', new Blob([JSON.stringify(request)], { type: 'application/json' }));
        images.forEach(file => formData.append('images', file));

        submittingRef.current = true;
        setIsSubmitting(true);
        try {
            // 함께 제공한 itemApi.js의 createItem은 ApiResponse.data에 담긴 상품 ID를 반환한다.
            // 등록 성공 후 상세 화면으로 이동하며, ID가 없으면 상품 목록으로 이동한다.
            const itemId = await createItem(formData);
            navigate(itemId != null ? `/items/${itemId}` : '/items', { replace: true });
        } catch (error) {
            // 실패하면 입력 상태와 선택한 파일을 유지해 내용을 다시 작성하지 않아도 된다.
            // 서버의 메시지가 문자열일 때만 화면에 표시한다.
            const message = error.response?.data?.message;
            setErrorMessage(typeof message === 'string' && message.trim()
                ? message : '상품 등록에 실패했습니다. 입력 내용과 연결 상태를 확인해 주세요.');
        } finally {
            // 성공·실패 어느 경우든 중복 제출 잠금을 정리한다.
            submittingRef.current = false;
            setIsSubmitting(false);
        }
    }

    return (
        <section className="item-page item-create-page" aria-labelledby="item-create-heading">
            <div className="item-container">
                <nav className="item-breadcrumb" aria-label="현재 위치">
                    <Link to="/items">상품 목록</Link><span aria-hidden="true">/</span>
                    <span aria-current="page">상품 등록</span>
                </nav>
                <header className="item-page-heading">
                    <div>
                        <p className="item-eyebrow">KIRBY WORLD · SHOP</p>
                        <h1 id="item-create-heading">새로운 상품을 소개해 주세요.</h1>
                        <p className="item-page-description">상품 정보와 이미지를 입력해 등록하세요.</p>
                    </div>
                </header>
                <form className="item-create-form" onSubmit={handleSubmit} aria-busy={isSubmitting}>
                    {errorMessage && <p className="item-create-error" role="alert">{errorMessage}</p>}
                    {/* fieldset으로 제출 중 입력·카테고리 버튼·파일 선택을 함께 잠근다.
                        label은 입력 id와 연결하고 필수 항목은 required로 표시한다. */}
                    <fieldset className="item-create-fields" disabled={isSubmitting}>
                        <legend className="item-sr-only">상품 등록 정보</legend>
                        <label htmlFor="create-name">상품명 <span>(필수)</span></label>
                        <input id="create-name" name="name" value={form.name} onChange={changeField}
                               maxLength={200} required placeholder="상품명을 입력해 주세요" />
                        <div className="item-create-grid">
                            <div>
                                <label htmlFor="create-price">가격 (원) <span>(필수)</span></label>
                                <input id="create-price" name="price" type="number" min="0" max={MAX_INTEGER} step="1"
                                       value={form.price} onChange={changeField} required placeholder="0" />
                            </div>
                            <div>
                                <label htmlFor="create-stock">재고 수량 (개) <span>(필수)</span></label>
                                <input id="create-stock" name="stockQuantity" type="number" min="0" max={MAX_INTEGER} step="1"
                                       value={form.stockQuantity} onChange={changeField} required placeholder="0" />
                            </div>
                        </div>
                        <label htmlFor="create-description">상품 설명 <span>(필수)</span></label>
                        <textarea id="create-description" name="description" rows={6} value={form.description}
                                  onChange={changeField} required placeholder="상품의 특징과 안내 사항을 입력해 주세요" />
                        <p className="item-create-label">카테고리 <span>(필수)</span></p>
                        {/* 기존 CATEGORY_TREE를 재사용한다. 선택 ID는 문자열로 보관하고 제출할 때 숫자로 변환한다. */}
                        <CategoryFilter value={form.categoryId}
                                        onChange={categoryId => setForm(previous => ({ ...previous, categoryId }))} />
                        <p className="item-create-help">카테고리를 펼쳐 원하는 분류를 선택해 주세요. 전체 상품은 등록 분류로 사용할 수 없습니다.</p>
                        <label htmlFor="create-images">상품 이미지 <span>(필수)</span></label>
                        <input id="create-images" type="file" accept="image/jpeg,image/png,image/webp" multiple
                               required onChange={handleImageChange} aria-describedby="create-images-help" />
                        <p id="create-images-help" className="item-create-help">JPG, PNG, WEBP 파일을 선택하세요. 선택 순서대로 전송됩니다.</p>
                        {images.length > 0 && <ul className="item-create-files">{images.map((file, index) => (
                            <li key={`${file.name}-${index}`}>{index + 1}. {file.name}</li>
                        ))}</ul>}
                    </fieldset>
                    <div className="item-create-actions">
                        <Link to="/items" className="item-button item-button-outline">목록으로</Link>
                        <button className="item-button item-button-primary" type="submit" disabled={isSubmitting}>
                            {isSubmitting ? '등록 중...' : '상품 등록'}
                        </button>
                    </div>
                    <span className="item-sr-only" role="status">{isSubmitting ? '상품과 이미지를 업로드하고 있습니다.' : ''}</span>
                </form>
            </div>
        </section>
    );
}

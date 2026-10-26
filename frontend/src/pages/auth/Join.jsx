import { useRef, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { authApi } from '@/services/api.js';
import { validateEmail, validatePassword } from '@/utils/validation.js';
import './Join.css';

// DTO의 8개 필드와 화면에서만 사용하는 비밀번호 확인값을 모두 초기화한다.
const INITIAL_FORM = {
  username: '',
  nickname: '',
  loginId: '',
  password: '',
  passwordConfirm: '',
  email: '',
  city: '',
  street: '',
  zipcode: '',
};

// 반복되는 입력창의 속성을 모은다. 필수 여부는 화면 정책이며,
// 현재 DTO에서 검증 어노테이션이 붙은 필드는 username뿐이다.
const ACCOUNT_FIELDS = [
  { name: 'username', label: '이름', autoComplete: 'name', required: true, placeholder: '이름을 입력해 주세요' },
  { name: 'nickname', label: '닉네임', autoComplete: 'nickname', placeholder: '사용할 닉네임을 입력해 주세요' },
  { name: 'loginId', label: '아이디', autoComplete: 'username', required: true, placeholder: '로그인에 사용할 아이디' },
  { name: 'email', label: '이메일', type: 'email', autoComplete: 'email', required: true, placeholder: 'example@email.com' },
  { name: 'password', label: '비밀번호', type: 'password', autoComplete: 'new-password', required: true, placeholder: '8자 이상 입력해 주세요' },
  { name: 'passwordConfirm', label: '비밀번호 확인', type: 'password', autoComplete: 'new-password', required: true, placeholder: '비밀번호를 한 번 더 입력해 주세요' },
];

const ADDRESS_FIELDS = [
  { name: 'city', label: '도시', autoComplete: 'address-level1', placeholder: '예: 서울특별시' },
  { name: 'zipcode', label: '우편번호', autoComplete: 'postal-code', placeholder: '예: 01234' },
  { name: 'street', label: '도로명·상세주소', autoComplete: 'address-line1', placeholder: '도로명과 상세주소를 입력해 주세요', wide: true },
];

export default function Join() {
  const navigate = useNavigate();
  const [formData, setFormData] = useState(INITIAL_FORM);
  const [errors, setErrors] = useState({});
  const [errorMessage, setErrorMessage] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  // state가 화면에 반영되기 전 연속 제출되는 경우까지 막기 위한 잠금이다.
  const submittingRef = useRef(false);
  const formRef = useRef(null);

  const handleChange = (event) => {
    const { name, value } = event.target;
    setFormData((previous) => ({ ...previous, [name]: value }));
    setErrorMessage('');
    setErrors((previous) => ({
      ...previous,
      [name]: '',
      // 원래 비밀번호가 바뀌면 확인값의 이전 오류도 다시 검사해야 한다.
      ...(name === 'password' ? { passwordConfirm: '' } : {}),
    }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    if (submittingRef.current) return;
    setErrorMessage('');

    // 서버로 전송할 DTO 필드만 명시한다. passwordConfirm은 전송하지 않는다.
    // 비밀번호의 공백은 실제 비밀번호의 일부일 수 있으므로 trim하지 않는다.
    const requestData = {
      username: formData.username.trim(),
      nickname: formData.nickname.trim(),
      loginId: formData.loginId.trim(),
      password: formData.password,
      email: formData.email.trim(),
      city: formData.city.trim(),
      street: formData.street.trim(),
      zipcode: formData.zipcode.trim(),
    };

    const nextErrors = {};
    if (!requestData.username) nextErrors.username = '이름을 입력해 주세요.';
    if (!requestData.loginId) nextErrors.loginId = '아이디를 입력해 주세요.';
    if (!requestData.email) {
      nextErrors.email = '이메일을 입력해 주세요.';
    } else if (!validateEmail(requestData.email)) {
      nextErrors.email = '올바른 이메일 주소를 입력해 주세요.';
    }
    if (!requestData.password.trim()) {
      nextErrors.password = '비밀번호를 입력해 주세요.';
    } else if (!validatePassword(requestData.password)) {
      nextErrors.password = '비밀번호는 8자 이상이어야 합니다.';
    }
    if (!formData.passwordConfirm) {
      nextErrors.passwordConfirm = '비밀번호 확인을 입력해 주세요.';
    } else if (requestData.password !== formData.passwordConfirm) {
      nextErrors.passwordConfirm = '비밀번호가 일치하지 않습니다.';
    }

    setErrors(nextErrors);
    if (Object.keys(nextErrors).length > 0) {
      // 검증 실패 시 요청을 중단하고 첫 오류 입력창으로 이동한다.
      const firstInvalidField = Object.keys(nextErrors)[0];
      formRef.current?.elements.namedItem(firstInvalidField)?.focus();
      return;
    }

    submittingRef.current = true;
    setIsLoading(true);

    try {
      // authApi는 Axios 응답을 그대로 반환하므로 response.data가 ApiResponse다.
      // 일반 api 인스턴스는 응답 인터셉터가 있어 반환 구조가 다르다.
      const response = await authApi.post('/api/auth/join', requestData);
      if (response.data?.success !== true) {
        setErrorMessage(response.data?.message || '회원가입을 완료하지 못했습니다.');
        return;
      }

      window.alert(response.data.message || '회원가입이 완료되었습니다.');
      navigate('/login', { replace: true });
    } catch (error) {
      // 실패해도 입력값은 보존하고 서버에서 전달한 중복 가입 등의 메시지를 표시한다.
      setErrorMessage(
          error.response?.data?.message || '회원가입에 실패했습니다. 잠시 후 다시 시도해 주세요.',
      );
    } finally {
      submittingRef.current = false;
      setIsLoading(false);
    }
  };

  // label과 input을 id로 연결하고, 오류 설명도 해당 입력창과 연결한다.
  const renderField = (field) => {
    const id = `join-${field.name}`;
    const error = errors[field.name];

    return (
        <div className={`join-page__field${field.wide ? ' join-page__field--wide' : ''}`} key={field.name}>
          <label htmlFor={id}>
            {field.label}
            <span className={field.required ? 'join-page__required' : 'join-page__optional'}>
            {field.required ? ' *' : ' (선택)'}
          </span>
          </label>
          <input
              id={id}
              name={field.name}
              type={field.type || 'text'}
              value={formData[field.name]}
              onChange={handleChange}
              placeholder={field.placeholder}
              autoComplete={field.autoComplete}
              required={field.required}
              aria-invalid={Boolean(error)}
              aria-describedby={error ? `${id}-error` : undefined}
          />
          {error && <p id={`${id}-error`} className="join-page__field-error">{error}</p>}
        </div>
    );
  };

  return (
      <section className="join-page" aria-labelledby="join-heading">
        <header className="join-page__header">
          <p className="join-page__eyebrow">WELCOME</p>
          <h1 id="join-heading">회원가입</h1>
          <p>기본 정보를 입력하고 새로운 이야기를 시작해 보세요.</p>
        </header>

        <form ref={formRef} className="join-page__card" onSubmit={handleSubmit} noValidate aria-busy={isLoading}>
          {/* noValidate로 브라우저 팝업 대신 위의 일관된 오류 문구를 사용한다.
            fieldset을 비활성화하면 요청 중 모든 입력과 제출 버튼이 함께 잠긴다. */}
          <fieldset className="join-page__fieldset" disabled={isLoading}>
            <legend>계정 정보</legend>
            <p className="join-page__hint">* 표시된 항목은 필수 입력입니다.</p>
            <div className="join-page__grid">{ACCOUNT_FIELDS.map(renderField)}</div>
          </fieldset>

          <fieldset className="join-page__fieldset join-page__fieldset--address" disabled={isLoading}>
            <legend>주소 정보 <span className="join-page__optional">(선택)</span></legend>
            <p className="join-page__hint">주소는 입력하지 않아도 가입할 수 있습니다.</p>
            <div className="join-page__grid">{ADDRESS_FIELDS.map(renderField)}</div>
          </fieldset>

          <div aria-live="polite" aria-atomic="true">
            {Object.keys(errors).some((name) => errors[name]) && (
                <p className="join-page__summary">입력 내용을 확인해 주세요. 각 항목 아래에 오류가 표시됩니다.</p>
            )}
            {errorMessage && <p className="join-page__summary" role="alert">{errorMessage}</p>}
          </div>

          <button className="join-page__submit" type="submit" disabled={isLoading}>
            {isLoading ? '가입 처리 중...' : '회원가입'}
          </button>

          <p className="join-page__login">이미 계정이 있으신가요? <Link to="/login">로그인</Link></p>
        </form>
      </section>
  );
}


import { useEffect, useRef } from 'react';
import { useAuth } from '../context/AuthContext';
import { injectAuthStore } from '../services/api';

/*
 * ============================================================
 * AxiosAuthInterceptor
 * ============================================================
 *
 * 역할:
 *
 * React의 AuthContext와 일반 JavaScript 모듈인
 * api.js를 연결하는 컴포넌트다.
 *
 * AuthContext:
 *   Access Token을 관리한다.
 *
 * api.js:
 *   HTTP 요청 전에 Access Token을 조회하고
 *   Silent Refresh 후 새로운 토큰을 저장한다.
 *
 * Axios 모듈(일반 자바스크립트파일)에서는 React Hook(useAuth등)을 직접 사용할 수 없으므로
 * 컴포넌트가 마운트될때 useEffect를 활용해 React의 상태 관리 함수를 비-React 영역으로 전달해 주는 통로 역할을 함
 * 두 영역을 연결하는 Bridge인셈.
 */

//AxiosAuthInterceptor는 화면에 어떤 버튼이나 디자인을 직접 그려내는 컴포넌트가 아니라
//오직 React(AuthContext)와 Axios를 뒤에서 몰래 연결(Bridge)해 주는 역할만 하므로
//화면을 직접 그리는 대신, 내부에 품고 있던 자식 컴포넌트들을 그대로 뱉어냄(렌더링)
//결과적으로: AxiosAuthInterceptor가 백그라운드에서 인증 연결 작업을 싹 세팅해 준 뒤,
//자신에게 감싸져 있던 children(실제 웹 화면들)을 아무런 방해 없이 그대로 화면에 렌더링해 주는 통로 역할을 하게 되는 것

export const AxiosAuthInterceptor = ({ children }) => {
    //리액트에서 컴포넌트를 만들 때, 태그 사이에 다른 컴포넌트나 HTML 요소를 감싸서 넘길 수 있는데
    // 이때 감싸인 내용들이 자동으로 children이라는 이름의 prop으로 전달
    //<AxiosAuthInterceptor>
    //     <App /> {/* 이 안쪽에 있는 모든 것들이 children이 됨! */}
    // </AxiosAuthInterceptor> 이런식으로 쓰면 AxiosAuthInterceptor 컴포넌트 내부로 App 컴포넌트가 children이라는 이름으로 전달됨

    /*
     * 1. AuthContext에서 현재 토큰과 갱신 함수 가져오기
     */
    const { accessToken, updateToken } = useAuth();

    /*
     * 2. Access Token을 ref에 보관
     *
     * useRef는 렌더링 사이에 값을 유지한다.
     *
     * useState와 달리 ref.current를 변경해도
     * 그 자체로 컴포넌트를 다시 렌더링하지 않는다.
     *
     * 따라서 Axios가 토큰을 조회할 때
     * 최신 값을 참조하기 위한 용도로 적합하다.
     */
    const tokenRef = useRef(accessToken);

    /*
     * 3. 렌더링할 때마다 최신 Access Token을 ref에 반영
     *
     * 로그인:
     *   accessToken = "eyJ..."
     *
     * 로그아웃:
     *   accessToken = null
     *
     * 토큰 재발급:
     *   accessToken = "새로운 JWT"
     *
     * Axios에 등록한 함수는 tokenRef.current를 읽으므로
     * 매번 다시 등록할 필요가 없다.
     */
    tokenRef.current = accessToken;

    /*
     * 4. AuthContext와 Axios 연결
     * useEffect의 두 번째 인수로 [updateToken]을 주어, updateToken 함수가 변경될 때만 이 연결 작업이 안전하게 수행되도록 제어함.
     * 만약 useEffect 없이 컴포넌트 본문 안에서 바로 injectAuthStore를 호출했다면, 컴포넌트가 렌더링될 때마다 매번 불필요하게 인터셉터 설정이 새로 등록되는 부하가 생길 수 있음.
     * 이를 방지하고 최초 마운트 시점에 딱 알맞게 초기화하는 안전장치 역할
     */

    //부모 컴포넌트인 AuthContext가 리렌더링되서 자식 컴포넌트인 AxiosAuthInterceptor가 리렌더링 되더라도
    //useEffect는 자기가 실행할 조건인 의존성 배열만을 보고 실행 여부를 판단함(그래서 단순히 리렌더링 됐다고 실행되는것이 아니라 무시되거나 스킵됨)
    useEffect(() => {

        /*
         * getToken:
         * Axios 요청 인터셉터가 호출할 함수
         *
         * 실제 토큰 문자열을 전달하는 것이 아니라
         * 최신 토큰을 조회하는 함수를 전달한다.
         */
        const getToken = () => tokenRef.current;

        /*
         * setToken:
         * Axios가 Silent Refresh에 성공했을 때
         * AuthContext의 Access Token을 변경한다.
         */
        const setToken = (newToken) => {
            updateToken(newToken);
        };

        /*
         * api.js의 모듈 변수에 두 함수를 등록한다.
         *
         * getAccessTokenFromMemory = getToken
         * updateAccessTokenInMemory = setToken
         */
        injectAuthStore(getToken, setToken);//이녀석이 api.js에 잇는 함수
        //각페이지들에선 api.js에서 설정해놓은 api를 쓰니까 이걸로 api.js에 값을 넘겨줘서 그걸 바탕으로 설정

    }, [updateToken]);
    //함수도 객체!컴포넌트가 리렌더링 되면 함수자체가 메모리상에서 새로만들어져 참조주소가 바뀌므로 함수가 변경되는거라 할수 잇음,
    //그러므로 함수(updateToken)의 코드가 새로 갱신되거나 재생성되면, useEffect를 다시 실행해서 api.js에 박혀있는 연결 함수도 최신 버전으로 갈아 끼워라"라는 의미로 의존성 배열에 넣어준 것

    /*
     * 5. 자식 컴포넌트를 그대로 렌더링한다.
     *
     * AxiosAuthInterceptor는 화면을 만드는 목적이 아니라
     * 인증 상태와 Axios를 연결하기 위한 컴포넌트다.
     */
    return children;
};

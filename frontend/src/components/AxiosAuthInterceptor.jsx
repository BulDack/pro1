//React의 useAuth 훅값을 비-컴포넌트 영역인 Axios모듈()로 브릿지(bridge)해주는 컴포넌트
//Axios 인터셉터와 리액트 라우터를 연결해주는 징검다리(기존의 history navigator역할)

import { useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { injectAuthStore } from '../services/api';

export const AxiosAuthInterceptor = ({ children }) => {
    const { accessToken, updateToken } = useAuth();

    useEffect(() => {
        // AuthContext의 메모리 접근자 함수들을 Axios 인스턴스에 주입
        injectAuthStore(
            () => accessToken,
            (newToken) => updateToken(newToken)
        );
    }, [accessToken, updateToken]);

    return children;
};
import React from 'react';
import { Link } from "react-router-dom";
import { useAuth } from "@/context/AuthContext.jsx";

function Mypage () {
    /*
    * AuthContext에서 현재 로그인한 사용자의 권한을 가져온다.
    *
    * role은 백엔드에서 조회한 사용자 정보나
    * 인증 상태에 연결되어 있어야 한다.
    *
    * 예: ADMIN, USER
    */
    const {user}=useAuth();


    return(
        <div>
            <h1>마이 페이지</h1>
            <p>
                {user?.nickname}님 환영합니다.
            </p>
            <p>jwt 인증에 성공한 유저만 들어올 수 있는 보안 페이지 입니다.</p>
            {user?.role === "ADMIN" && (
            <Link to="/admin/items/new">
                상품 등록
            </Link>
                )}
        </div>
    );
};

export default Mypage;
// import React from 'react';
// import './HomePage.css';
//
// const HomePage = () => {
//     return (
//         <div>
//             <h1>메인 페이지</h1>
//             <p>누구나 접근 가능한 공개 페이지입니다.</p>
//             <img
//                 src="http://localhost:8080/uploads/items/muck.png"
//                 alt="상품 이미지"
//                 style={{
//                     width: '300px',
//                     height: '300px',
//                     objectFit: 'cover'
//                 }}
//             />
//         </div>
//
//     );
// };
//
// export default HomePage;

import React from 'react';
import './HomePage.css';

const HomePage = () => {
    return (
        <div className="home-page">
            <div className="home-container">
                <section className="hero-section">
                    <div className="hero-text">
                        <p className="hero-label">
                            Welcome to KirbyWorld~
                        </p>

                        <h1 className="hero-title">
                            일상에 작은 설렘을
                            <br />
                            찾아보세요.
                        </h1>

                        <p className="hero-description">
                            다양한 상품을 둘러보고
                            <br />
                            마음에 드는 상품을 만나보세요.
                        </p>

                        <button className="hero-button">
                            상품 보러가기
                        </button>
                    </div>

                    <div className="hero-image-wrapper">
                        <img
                            src="http://localhost:8080/uploads/items/muck.png"
                            alt="상품 이미지"
                            className="hero-image"
                        />
                    </div>
                </section>
            </div>
        </div>
    );
};

export default HomePage;

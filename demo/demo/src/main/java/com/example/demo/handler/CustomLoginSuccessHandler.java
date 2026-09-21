package com.example.demo.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;
//RequestCacheAwareFilter는 스프링 시큐리티 필터 체인에 기본적으로 포함되어 있기 때문에,
// 핸들러를 직접 구현하지 않아도 시큐리티의 기본 설정만으로 가려던 페이지를 기억해낼 수 있음.
//기본 필터만 써도 가려던 페이지로 잘 보내주지만, 직접 핸들러를 구현하는 이유는 보통 다음과 같은 추가 로직이 필요하기 때문입니다.

//로그인 성공 시 세션에 특정 객체(Member 등)를 직접 넣어주고 싶을 때
//로그인 성공 횟수를 DB에 기록하고 싶을 때
//로그인 시점에 따라 사용자별로 서로 다른 환영 메시지를 보여주고 싶을 때
@Component
public class CustomLoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    //인증 절차 문제로 리다이렉트 된 후에 이전에 했던 요청 정보를 담고 있는 'SavedRequest’ 객체를 쿠키 혹은 세션에 저장하고
    // 필요시 다시 가져와 실행하는 캐시
    private final RequestCache requestCache=new HttpSessionRequestCache();

    //시큐리티의 ExceptionTranslationFilter가 현재 요청 정보를 RequestCache에 저장합니다.
    //로그인: 사용자가 로그인 페이지로 리다이렉트되어 로그인을 완료합니다.
    //복원: SavedRequestAwareAuthenticationSuccessHandler가 캐시된 정보를 확인하고 사용자를 /items/new로 보냅니다.

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        // 1. 캐시된 이전 요청 정보를 꺼냅니다.
        SavedRequest savedRequest=requestCache.getRequest(request,response);
        // 2. 만약 이전에 가려던 페이지가 있다면 그곳으로 보내줍니다.
        if(savedRequest!=null){
            String targetUrl=savedRequest.getRedirectUrl();
            getRedirectStrategy().sendRedirect(request,response,targetUrl);
            return;
        }
        // 3. 이전 정보가 없다면 기본 홈 화면으로 보냅니다.
        setDefaultTargetUrl("/loginHome");
        super.onAuthenticationSuccess(request, response, authentication);
    }
}

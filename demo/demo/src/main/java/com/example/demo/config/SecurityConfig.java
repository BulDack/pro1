package com.example.demo.config;

import com.example.demo.handler.CustomLoginSuccessHandler;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

//    private final CustomLoginSuccessHandler successHandler;
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http.authorizeHttpRequests(auth->auth
//                        .requestMatchers("/login","/members/logout-success","/members/new","/css/**","/").permitAll()
//                        .anyRequest().authenticated())
//                .formLogin(form->form
//                        .loginPage("/Login")
//                        .usernameParameter("loginId")
//                        .loginProcessingUrl("/login")
//                        .defaultSuccessUrl("/loginHome")
//                        .successHandler(successHandler)
//                        .permitAll()
//                )
//                .logout(logout->logout
//                        .logoutUrl("/logout")
//                        .logoutSuccessUrl("/members/logout-success") // 로그아웃 성공 시 이동할 URL
//                        .invalidateHttpSession(true)
//                        .deleteCookies("JSESSIONID")
//                        .clearAuthentication(true)   // 시큐리티 인증 정보 초기화
//                        .permitAll()
//                )
//                .requestCache((cache)-> cache.requestCache(new HttpSessionRequestCache())
//                )
//                .sessionManagement(session->session
//                        .sessionFixation(sessionFixation->sessionFixation.changeSessionId())//기존세션을 유지하면서 세션id만 변경하여
//                        //인증과정에서 세션 고정공격을 방지하는 방식(기본값)
//                        .invalidSessionUrl("/") //이미 만료된세션으로 요청하는 사용자를 리다이렉션
//                        .maximumSessions(1)//사용자당 최대세션수를 제어,기본값은 무제한
//                        .expiredUrl("/") // 세션을 만료하고 나서 리다이렉션 할 URL 을 지정한다.
//
//                )
//
//
//
//        ;
//        return http.build(); //이미 만료된세션으로 요청하는 사용자를 리다이렉션
//    }
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomOAuth2UserService customOAuth2UserService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                //리액트(5173)과 스프링(8080)간의 교차출입을 허용하는 cors 설정 연결
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                //리액트가 헤더나 Body로 통신하므로 CSRF 보호 기능은 비활성화
                .csrf(csrf -> csrf.disable())
                //기존의 Form 로그인창 및 기본 HTTP 로그인창 비활성화 (리액트가 직접 그릴 거니까)
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                // 시큐리티 기본 로그아웃 비활성화
                .logout(logout -> logout.disable())

                //JWT를 쓸 것이므로 세션을 서버에 저장하지 않는 STATELESS(무상태) 설정
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // URL별 접근 권한 설정 (🔥 주소 앞에 '/' 추가 필수!)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/admin/**").hasRole("ADMIN") //이주소는 관리자만
                        .requestMatchers("/api/auth/login", "/api/auth/join").permitAll()// 'api/...' -> '/api/...'로 수정
                        .anyRequest().authenticated())

                //소셜로그인 설정
                .oauth2Login(oauth2->oauth2
                        .loginPage("/login")
                        .userInfoEndpoint(userInfo->userInfo
                                //OAuth2 로그인 성공후 사용자 정보를 가져올 때 이 서비스를 사용
                                .userService(customOAuth2UserService)
                        )
                        //로그인이 성공하면 우리가 만든 핸들러가 가로채서 JWT를 구워 넘겨주도록 등록!
                        //최초로그인성공시에만 딱1번 실행되고,로그인 이후 서비스 이용시부터는
                        //매 api요청마다(stateless이기때문에 security context를 매번 리셋해서 토큰 저장) 기존방식과 똑같이 JwtAuthenticationFilter로 처리함
                        //
                        .successHandler(oAuth2LoginSuccessHandler)
                )

                // 4. 🔥 [핵심] UsernamePasswordAuthenticationFilter 전에 내가 만든 JWT 필터를 꽂아넣기!!!
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)



                // 5. 예외 처리 설정 =======이부분 잘모르겠음 다시 한번 보기!!!!!!!
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"success\": false, \"message\": \"로그인이 필요한 서비스입니다.\"}");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"success\": false, \"message\": \"접근 권한이 없습니다.\"}");
                        })
                );

        return http.build();
    }

    //리액트와 연동할때 안켜면 무조건 에러나는 cors 설정 빈 == 여기도 아직 이해못함!!!!!!!
    @Bean
    private CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 내 리액트 서버의 주소를 정확히 허용 (포트 번호 주의!)
        // ⭐️ 1. 프론트엔드 URL 허용 (리액트 개발 서버 포트)
        configuration.setAllowedOrigins(List.of("http://localhost:5173", "http://localhost:3000"));
        // 2. 허용할 HTTP 메서드 지정
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        // 3. 허용할 요청 헤더 지정
        configuration.setAllowedHeaders(List.of("*"));
        // ⭐️ 4. 자격 증명 허용 (쿠키, Authorization 헤더 등을 주고받기 위해 필수)
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

package com.example.demo.web.controller;

import com.example.demo.dto.JoinRequestDto;
import com.example.demo.dto.login.LoginDto;
import com.example.demo.dto.security.PrincipalDetails;
import com.example.demo.service.MemberService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/경로 아직 미설정")
public class MemberController {

    private final MemberService memberService;

    //회원가입 api
    //ResponseEntitys는 화면에 보여줄 데이터가 없을 때만 쓸 수 있음,
    @PostMapping("/join")
    public ResponseEntity<String>join(@RequestBody @Valid JoinRequestDto requestDto){
        memberService.joinMember(requestDto);
        return ResponseEntity.ok("회원가입이 완료되었습니다.");
    }

    //로그인 api
    @PostMapping("/login")
    public ResponseEntity<LoginDto.AccessResponseDto>login(@RequestBody LoginDto.RequestDto requestDto,
                                                     HttpServletResponse response){

        // 1.서비스에서 로그인 로직 수행 후 토큰 및 회원 정보 생성
        LoginDto.ResponseDto responseDto=memberService.login(requestDto);

        // 2. 서버측에서 refreshToken을 담은 httpOnly 쿠키 생성
        Cookie refreshTokenCookie=new Cookie("refreshToken",responseDto.getRefreshToken());
        refreshTokenCookie.setHttpOnly(true); // 자바스크립트 접근 차단 (XSS 방어)
        //refreshTokenCookie.setSecure(true); // HTTPS 환경에서만 쿠키 전송 (실무 필수)
        refreshTokenCookie.setPath("/"); // 모든 URL 요청에 이 쿠키가 포함되도록 설정
        refreshTokenCookie.setMaxAge(7 * 24 * 60 * 60); //쿠키 유효기간 설정 (예:7일)

        // 3. 응답 헤더에 쿠키 주입
        response.addCookie(refreshTokenCookie);

        // 4. Access Token만 담은 깔끔한 DTO로 프론트엔드에게 응답 바디 전달
        LoginDto.AccessResponseDto reponseBody= LoginDto.AccessResponseDto.builder()
                .accessToken(responseDto.getAccessToken())
                .username(responseDto.getUsername())
                .build();

        // 로그인 성공 정보(JSON)와 함께 200 OK 반환
        return ResponseEntity.ok(reponseBody);
    }

    //로그아웃 api

    //로그아웃할 때는 DB/Redis에서 토큰을 지우는 것뿐만 아니라, 브라우저에 남아있는 쿠키도 같이 만료(삭제)시켜 주어야 함
    //쿠키를 지우는 방법은 의외로 간단한데, 유효기간(MaxAge)을 0으로 세팅한 똑같은 이름의 빈 쿠키를 응답으로 덮어씌워 보내주면 브라우저가 알아서 쿠키를 증발시킴.
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@AuthenticationPrincipal PrincipalDetails principalDetails,
                                       @CookieValue(value = "refreshToken", required = false) String refreshToken,
                                         HttpServletResponse response){ // 👈 쿠키 삭제를 위해 response 필요

        // 1.서비스단에서 우리db의 refreshToken 삭제
        if(principalDetails !=null){
            memberService.logout(refreshToken,principalDetails.getId());
        }

        // 2. ⭐️ 브라우저의 쿠키를 지우기 위해 만료시간이 0인 쿠키를 밀어 넣음
        Cookie deleteCookie = new Cookie("refreshToken",null);
        deleteCookie.setHttpOnly(true);
        deleteCookie.setPath("/");
        deleteCookie.setMaxAge(0); //유효시간을 0으로 설정하여 즉시 삭제 처리

        response.addCookie(deleteCookie);

        return ResponseEntity.ok("로그아웃이 완료되었습니다.");
    }


//    @GetMapping(value="/members/new")
//    public String createForm(Model model){
//        model.addAttribute("memberForm",new MemberForm()); //validation을 하기위해 빈 껍데기라도 보내줌
//        return "members/createMemberForm";
//    }
//    @PostMapping(value="/members/new")
//    public String create(@Valid MemberForm form, BindingResult result, HttpServletRequest request){
//
//        if(result.hasErrors()){
//            return "members/createMemberForm";
//        }
//        Address address=new Address(form.getCity(),form.getStreet(),form.getZipcode());
//        Member member=createMember(form,address);
//        memberService.join(member);
//
//        HttpSession session=request.getSession();
//        session.setAttribute("member",member);
//
//        return "redirect:/loginHome"; //여기서 담긴 값은 이 메서드 안에서만 유효합니다.
//        // redirect를 해버리면 브라우저는 새로운 요청을 보내기 때문에, 이전 메서드에서 담았던 값은 사라집니다.
//    }
//    @GetMapping("/loginHome")
//    public String loginHome(Member member) {
//        return "loginHome"; // templates/loginHome.html 파일을 찾음
//    }
//
//    @PostMapping(value="/members/update/{id}")
//    public UpdateMemberResponse update(@PathVariable("id")Long id, @RequestBody @Valid UpdateMemberRequest request){
//
//        memberService.update(id,request.getName());
//        Member updateMember=memberService.findMember(id);
//        return new UpdateMemberResponse(updateMember.getUsername(),updateMember.getId());
//    }
//
//    @GetMapping(value="/login")
//    public String loginForm(Model model){
//        model.addAttribute("memberForm",new MemberForm());
//        return "members/login";
//    }
//
//    @GetMapping("/members/logout-success")
//    public String logoutSuccessPage() {
//        return "/members/logoutSuccess"; // HTML 파일명이 logoutSuccess.html 인 경우
//    }


//    @PostMapping(value="/logout")
//    public String logout(HttpServletRequest request){
//        HttpSession session =request.getSession();
//        if(session!=null){
//            session.invalidate();
//        }
//        return "redirect:/";
//    }

//    @Data
//    static class UpdateMemberRequest{
//        private String name;
//    }
//    @Data@AllArgsConstructor
//    static class UpdateMemberResponse{
//        private String name;
//        private Long id;
//    }
}

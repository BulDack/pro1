package com.example.demo.service;

import com.example.demo.config.JwtTokenProvider;
import com.example.demo.dto.JoinRequestDto;
import com.example.demo.dto.login.LoginDto;
import com.example.demo.dto.login.MypageDto;
import com.example.demo.entity.Address;
import com.example.demo.entity.Member;
import com.example.demo.entity.RefreshToken;
import com.example.demo.entity.ennum.Role;
import com.example.demo.repository.MemberRepository;
import com.example.demo.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static com.example.demo.dto.my.MyResponse.MemberResponse;



@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {


    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional //회원가입
    public Long joinMember(JoinRequestDto requestDto){
        Address address=new Address(requestDto.getCity(),requestDto.getStreet(),requestDto.getZipcode());
        Member member=Member.createMember(requestDto,address, Role.ROLE_USER);
        validateDuplicateMember(member);
        String encodedPassword=passwordEncoder.encode(member.getPassword());
        member.updatePassword(encodedPassword);
        memberRepository.save(member);
        return member.getId();
    }
    //실무에서는 검증 로직이 있어도 멀티 쓰레드 상황을 고려해서
    //회원 테이블의 회원명 컬럼에 유니크 제약 조건을 추가하는 것이 안전하다.

    //회원중복검증
    private void validateDuplicateMember(Member member){
        Optional<Member> findMembers= memberRepository.findByLoginId(member.getLoginId());
        if(findMembers.isPresent()){
            throw new IllegalStateException("이미 존재하는 회원입니다");
        }
    }

    //소셜 로그인은 컨트롤러를 직접 만들지 않음.스프링 시큐리티가 백그라운드에서 다 처리(그렇기 때문에 SUCCESSHANDER가 필요)
    @Transactional
    public LoginDto.ResponseDto login(LoginDto.RequestDto requestDto) {

        Member member= memberRepository.findByLoginId(requestDto.getLoginId())
                .orElseThrow(()->new IllegalArgumentException("아이디 또는 비밀번호가 일치하지 않습니다"));

        if(!passwordEncoder.matches(requestDto.getPassword(),member.getPassword())){
            throw new IllegalArgumentException("아이디 또는 비밀번호가 일치하지 않습니다");
        }

//        // 재발급과 로그아웃도 토큰을 조회·변경하기 전에 같은 잠금을 사용해야 합니다.(이부분 아직 잘)
//        memberRepository.findByIdForUpdate(member.getId())
//                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

        //인증이 완료되었으므로 jwt토큰 발급
        //엑세스토큰 생성
        String accessToken= jwtTokenProvider.createAccessToken(member);
        //리프레시 토큰도 생성
        String refreshToken= jwtTokenProvider.createRefreshToken(member);

        // 발급한 Refresh Token의 실제 만료 시각을 읽음.
        // JWT와 DB의 만료일을 동일하게 유지하기 위해
        // '현재 시각 + 7일'을 별도로 계산하지 않음.
        LocalDateTime expiryDate = LocalDateTime.ofInstant(
                jwtTokenProvider.getClaims(refreshToken)
                        .getExpiration()
                        .toInstant(),
                java.time.ZoneId.systemDefault()
        );


        // 새 로그인에서 발급한 토큰으로 교체합니다.
        // 이전 기기가 가진 Refresh Token으로는 더 이상 재발급할 수 없습니다.

        RefreshToken savedToken = refreshTokenRepository
                .findByMemberId(member.getId())
                .orElse(null);

        if(savedToken != null){
            // 새 로그인에서 발급한 토큰으로 교체합니다.
            // 이전 기기가 가진 Refresh Token으로는 더 이상 재발급할 수 없습니다.
            savedToken.updateToken(refreshToken, expiryDate);

            // login()에 @Transactional이 있으므로,
            // 조회한 엔티티의 변경 내용은 트랜잭션 종료 시 DB에 반영됩니다.
        } else {
            // 최초 로그인이나 로그아웃 후 로그인은 새 행을 생성합니다.
            RefreshToken newToken = RefreshToken.builder()
                    .token(refreshToken)
                    .memberId(member.getId())
                    .isUsed(false)
                    .expiryDate(expiryDate)
                    .build();

            refreshTokenRepository.save(newToken);
        }

        //리액트 프론트엔드가 필요한 정보들을 dto에 담아서 반환
        return LoginDto.ResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .username(member.getLoginId()) //화면에 ooo님 환영합니다와 같은 용도로 사용가능
                .build();
    }

//---JPA에서 deleteBy... 메서드를 호출하면 내부적으로 select 쿼리를 실행해 엔티티를 영속성 컨텍스트로 가져온 뒤 하나씩 delete 쿼리를 날립니다.
// 즉, 어차피 내부적으로 조회가 일어나기 때문에 차라리 개발자가 눈으로 보게끔 명시적으로 findByToken을 하는 것이 영속성 컨텍스트의 이점을 온전히 누리면서 비즈니스 로직을 통제하기에 훨씬 유리합니다.
    @Transactional
    public void logout(String refreshToken,Long loginId){

        //1.토큰 유효성 검증
        if(refreshToken ==null && !jwtTokenProvider.validateToken(refreshToken)) {
            throw new BadCredentialsException("유효하지 않거나 만료된 토큰입니다.");
        }
        //2. 해당토큰이 db에 살아있는지 조회
        RefreshToken tokenEntity=refreshTokenRepository.findByToken(refreshToken)
                        .orElseThrow(()->new IllegalArgumentException("이미 로그아웃되거나 존재하지 않는 토큰입니다. "));
        //3. 토큰은 있지만 다른사람의 기기 토큰일수도 있으므로 memberId가 일치하는지 최종검즘
        if(tokenEntity.getMemberId() != loginId){
            throw new AccessDeniedException("잘못된 접근입니다.");
        }
        refreshTokenRepository.delete(tokenEntity);
    }
    /*프론트엔드(React 등)와 협업 시 주의할 점

       서버 코드가 이렇게 구현되면, 로그아웃 API가 호출되었을 때 백엔드 저장소에서 Refresh Token이 증발합니다.
       이때 프론트엔드 개발자에게 반드시 아래 작업을 같이 요청하셔야 완벽한 로그아웃이 성립됩니다.
       백엔드의 /api/auth/logout API를 호출하여 성공 응답을 확인한다.
       브라우저의 LocalStorage 또는 Cookie에 저장해 두었던 accessToken과 refreshToken을 싹 지워준다(removeItem).
       로그인 페이지나 메인 페이지로 사용자를 이동(Redirect)시킨다.
       이렇게 양쪽에서 토큰을 지워주어야 사용자가 브라우저를 새로고침하거나 다음 API를 보낼 때 빈 헤더로 요청이 날아가며 안전하게 로그아웃 상태가 유지됩니다!
     */


    public List<Member> findMembers() {
        return memberRepository.findAll();
    }

    public Member findMember(Long memberId){
        return memberRepository.findById(memberId).orElseThrow(()->new NoSuchElementException("해당회원을 찾을 수 없읍니다"));
    }

    @Transactional
    public void update(Long id, String userName) {
        Member member= memberRepository.findById(id)
                .orElseThrow(()->new NoSuchElementException("해당회원을 찾을 수 없읍니다"));
        member.changeName(userName);

    }


    public MemberResponse getMe(Long memberId) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() ->
                        new IllegalArgumentException("회원을 찾을 수 없습니다.")
                );

        return new MemberResponse(member.getNickname());//이거 아직 안함
    }
}

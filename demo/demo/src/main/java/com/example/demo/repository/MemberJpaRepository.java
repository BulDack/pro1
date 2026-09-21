package com.example.demo.repository;

import com.example.demo.entity.Member;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
//테이블의 기본키(@Id)가 아닌 일반 컬럼이나 복합적인 조건으로 데이터를 다룰 때는 공통 메서드로 만들 수 없기 때문에, 규칙에 맞게 개발자가 직접 메서드를 정의해야함
public interface MemberJpaRepository extends JpaRepository<Member,Long> {

//    @PersistenceContext
//    private EntityManager em;


//    public List<Member> findAll(){
//        return em.createQuery("select m from member m",Member.class).getResultList();
//    }
    Optional<Member>findByLoginId(String loginid);

    //!! @Query 어노테이션을 이용한 JPQL / 네이티브 쿼리
    //조건이 너무 복잡해서 메서드 이름이 무한정 길어지거나 성능 최적화가 필요할 때 사용합니다.
    // JPQL을 직접 작성하여 필요한 데이터만 조인해서 조회
    //ex) @Query("SELECT m FROM Member m JOIN FETCH m.team WHERE m.role = :role")
    //    List<Member> findMembersWithTeam(@Param("role") Role role);


    Optional<Member> findByEmail(String email);

//    public List<Member> findByName(String name){
//        return em.createQuery("select m from member m where m.name=:name",Member.class)
//                .setParameter("name",name)
//                .getResultList();
//    }
}

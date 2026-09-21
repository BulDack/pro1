package com.example.demo.repository;

import com.example.demo.entity.item.Item;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    // SELECT ... FOR UPDATE 쿼리가 실행되어 해당 Row에 X-Lock을 겁니다.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Item i WHERE i.id = :id")
    Optional<Item> findByIdWithPessimisticLock(@Param("id") Long itemId);

    // 여러 상품 조회 시 동시에 락 적용
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Item i WHERE i.id IN :ids")
    List<Item> findAllByIdInWithPessimisticLock(@Param("ids") List<Long> ids);



//    private final EntityManager em;
//
//    public void save(Item item)
//        if(item.getId()==null){
//            em.persist(item);
//        }else{
//            em.merge(item);
//        }
//    }
//    public Item findOne(Long id){
//        return em.find(Item.class,id);
//    }
//    public List<Item> findAll(){
//        return em.createQuery("select i from item i",Item.class).getResultList();
//    }


}

package com.example.demo.entity.item;

import com.example.demo.Exception.NotEnoughStockException;
import com.example.demo.convert.ItemDetailConverter;
import com.example.demo.entity.ItemCategory;
import com.example.demo.entity.base.BaseEntity;
import com.example.demo.entity.ennum.ItemStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.List;


//@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
//@DiscriminatorColumn(name="dtype")
//@SuperBuilder
@Entity
@SQLDelete(sql="UPDATE item SET status='DELETED' WHERE id = ?") //repository.delete(item)을 호출할 때, 실제 DELETE 쿼리 대신 우리가 지정한 UPDATE 쿼리가 나감 개발자는 평소처럼 delete()를 쓰면 되서 직관적.
@SQLRestriction("status='ACTIVE'")//repository.findAll()이나 findById()로 조회할 때, JPA가 자동으로 뒤에 WHERE status = 'ACTIVE'를 붙여서 쿼리를 날려줌. 즉, 이미 삭제된 데이터는 신경 쓰지 않아도 알아서 조회 대상에서 제외
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
//@AttributeOverride(
//        name = "updatedAt", // 부모의 필드명
//        column = @Column(name = "member_updated_date") // 바꿀 컬럼명
//)
public class Item extends BaseEntity {

    @Id@GeneratedValue
    @Column(name="item_id")
    private Long id;

    private String name;
    private int price;
    private int stockQuantity;
    private String description;

    @Enumerated(EnumType.STRING)
    private ItemStatus status= ItemStatus.ACTIVE; //초기값은 활성상태

    @Convert(converter= ItemDetailConverter.class)
    @Column(columnDefinition = "json") //db의 json타입 사용
    //화면 노출용 자질구레한 데이터는 JSON으로 유연하게
    private ItemDetailSpec detailSpec;
//    //@SuperBuilder나 @Builder를 사용하면 롬복이 객체를 생성할 때 우리가 직접 쓴 new ArrayList<>() 같은 초기화 식을 무시해 버립니다.
//    // 즉, 빌더로 객체를 만들면 categoryItems는 초기값인 빈 리스트가 아니라 **null**이 들어가게 됩니다.
//    @OneToMany(mappedBy = "item")
//    @Builder.Default // 이 어노테이션을 붙이면 초기화 식이 유지됩니다.
    @OneToMany(mappedBy = "item")
    private List<ItemCategory> itemCategorys=new ArrayList<>();

    @OneToMany(mappedBy = "item",cascade = CascadeType.ALL,orphanRemoval = true)
    private List<ItemSearchAttribute> searchAttributes=new ArrayList<>();

    public void addItemCategory(ItemCategory itemCategory){
        itemCategorys.add(itemCategory);
        itemCategory.addItem(this);
    }
    public void addStringSearchAttribute(String key,String value){
        ItemSearchAttribute attribute=ItemSearchAttribute.createStringAttribute(this,key,value);
        this.searchAttributes.add(attribute);
    }
    public void addNumberSearchAttribute(String key,Integer value){
        ItemSearchAttribute attribute=ItemSearchAttribute.createNumberAttribute(this,key,value);
        this.searchAttributes.add(attribute);
    }

    public static Item createItem(String name,int price,int stockQuantity,ItemCategory itemCategory,String description){
        Item item=new Item();
        item.name=name;
        item.price=price;
        item.stockQuantity=stockQuantity;
        item.description=description;
        item.addItemCategory(itemCategory);
//        for(ItemCategory itemCategory:itemCategories){
//            item.addItemCategory(itemCategory); //연관관계메서드
//        }
        return item;
    }
    //업데이트 메서드들
    public void changeItem(String name, int price) {
        this.name=name;
        this.price=price;
    }

    public void updateAttributes(List<ItemSearchAttribute>newAttributes){
        this.searchAttributes.clear(); //고아객체제거 기능으로 인해 자동으로 delete쿼리가 예약됨
        this.searchAttributes.addAll(newAttributes);
    }

    ////비즈니스 로직
    public void addStock(int quantity){
        this.stockQuantity += quantity;
    }
    public void removeStock(int quantity){
        int restStock=this.stockQuantity-quantity;
        if(restStock<0){
            throw new NotEnoughStockException("재고가 부족합니다");
        }
        this.stockQuantity=restStock;
    }
    public void deleteItem(){
        this.status=ItemStatus.DELETE;
        super.setDeleteDate();
    }

    public void changeStatus(ItemStatus status){
        this.status=status;
    }


}

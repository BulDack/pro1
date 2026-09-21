package com.example.demo.entity;

import com.example.demo.entity.ennum.DeliveryStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Delivery {

    @Id@GeneratedValue
    @Column(name="delivery_id")
    private Long id;

    @OneToOne(mappedBy = "delivery",fetch = FetchType.LAZY)
    private Order order;

    @Embedded
    private Address address;

    @Enumerated(EnumType.STRING)
    private DeliveryStatus deliveryStatus;


    public static Delivery createDelivery(Address address,DeliveryStatus deliveryStatus){
        Delivery delivery=new Delivery();
        delivery.address=address;
        delivery.deliveryStatus=deliveryStatus;
        return delivery;
    }
    public void setOrder(Order order) {
        this.order=order;
    }
}

package jpabook.jpashop.domain;


import lombok.Getter;

import javax.persistence.*;

@Entity
@Getter
public class Delivery {
    @Id
    @GeneratedValue
    @Column(name = "delivery_id")
    private Long id;

    @OneToOne(mappedBy = "delivery", fetch = FetchType.LAZY)
    private Order order;

    @Embedded
    private Address address;

    @Enumerated(EnumType.STRING) //무조건 STRING
    private DeliveryStatus status; //READY, COMP

    public void setOrder(Order order) {
        this.order = order;
    }
}

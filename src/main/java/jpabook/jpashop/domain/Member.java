package jpabook.jpashop.domain;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder //setter 대신 builder를 사용했다.
public class Member {

    @Id
    @GeneratedValue
    @Column(name = "member_id")
    private Long id;

    @NotEmpty
    private String name;

    @Embedded
    private Address address;

    @OneToMany(mappedBy = "member") //Order 테이블에 있는 member 필드에 의해서 매핑 된다는 뜻(연관관계 주인은 order)
    private List<Order> orders = new ArrayList<>();

    public void updateMember(String name, Address address) {
        this.name = name;
        this.address = address;
    }
}

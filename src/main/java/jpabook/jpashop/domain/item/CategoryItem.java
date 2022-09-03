package jpabook.jpashop.domain.item;

import jpabook.jpashop.domain.Category;
import lombok.Getter;

import javax.persistence.*;

/**
 * ManyToMany를 실무에서 쓰지 않길 권장한다고 해서
 * CategoryItem 엔티티를 직접 만들어서 ManyToOne OneToMany로 Item과 Category를 엮었다.
 */
@Entity
@Getter
public class CategoryItem {
    @Id
    @GeneratedValue
    @Column(name = "category_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;
    

}

package jpabook.jpashop.domain;

import jpabook.jpashop.domain.item.CategoryItem;
import lombok.Getter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
public class Category {

    @Id
    @GeneratedValue
    @Column(name = "category_id")
    private Long id;

    private String name;

    @OneToMany(mappedBy = "category")
    private List<CategoryItem> categoryItem = new ArrayList<>();
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @OneToMany(mappedBy = "parent")
    private List<Category> child = new ArrayList<>();

    //===== 연관관계 메서드 =====
    public void addChildCategory(Category child) {
        this.child.add(child);
        child.setParent(this);
    }

    private void setParent(Category category) {
        this.parent = category;
    }

}

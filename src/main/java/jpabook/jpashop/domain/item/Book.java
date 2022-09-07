package jpabook.jpashop.domain.item;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@SuperBuilder
public class Book extends Item {
    private String author;
    private String isbn;
}

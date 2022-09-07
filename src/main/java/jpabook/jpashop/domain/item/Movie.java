package jpabook.jpashop.domain.item;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("")
@Getter
@NoArgsConstructor
@SuperBuilder

public class Movie extends Item {
    private String director;
    private String actor;
}

package jpabook.jpashop.domain.item;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("")
@NoArgsConstructor
@Getter
@SuperBuilder
public class Album extends Item {
    private String artist;
    private String etc;
}

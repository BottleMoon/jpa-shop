package jpabook.jpashop.domain;


import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
public class Address {

    public String city;

    public String street;

    public String zipcode;
}

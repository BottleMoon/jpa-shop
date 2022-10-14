package jpabook.jpashop.service;

import jpabook.jpashop.domain.*;
import jpabook.jpashop.domain.item.Book;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;


/**
 * 총 주문 2개
 * * userA
 * * * JPA1 BOOK
 * * * JPA2 BOOK
 * * userB
 * * * SPRING1 BOOK
 * * * SPRING2 BOOK
 */
@Component
public class InitDb {

    private final InitService initService;

    public InitDb(InitService initService) {
        this.initService = initService;
    }

    @PostConstruct
    public void init() {
        initService.dbInit1();
        initService.dbInit2();
    }

    @Component
    @Transactional
    static class InitService {

        private final EntityManager em;

        InitService(EntityManager em) {
            this.em = em;
        }

        public void dbInit1() {
            Member member = createMember("userA", "부산", "1", "111");
            em.persist(member);

            Book book1 = createBook("JPA1 BOOK", 10000, 100);
            em.persist(book1);

            Book book2 = createBook("JPA2 BOOK", 20000, 100);
            em.persist(book2);

            OrderItem orderItem1 = OrderItem.createOrderItem(book1, 10000, 1);
            OrderItem orderItem2 = OrderItem.createOrderItem(book2, 20000, 2);

            Delivery delivery = createDelivery(member);
            Order order = Order.createOrder(member, delivery, orderItem1, orderItem2);

            em.persist(order);

        }

        public void dbInit2() {
            Member member = createMember("userB", "서울", "2", "222");
            em.persist(member);

            Book book1 = createBook("SPRING1 BOOK", 20000, 200);
            em.persist(book1);

            Book book2 = createBook("SPRING2 BOOK", 40000, 300);
            em.persist(book2);

            OrderItem orderItem1 = OrderItem.createOrderItem(book1, 10000, 3);
            OrderItem orderItem2 = OrderItem.createOrderItem(book2, 20000, 4);

            Delivery delivery = createDelivery(member);
            Order order = Order.createOrder(member, delivery, orderItem1, orderItem2);
            em.persist(order);

        }

        private static Delivery createDelivery(Member member) {
            Delivery delivery = new Delivery();
            delivery.setAddress(member.getAddress());
            return delivery;
        }

        private static Book createBook(String name, int price, int stockQuantity) {
            Book book = Book.builder()
                    .name(name)
                    .price(price)
                    .stockQuantity(stockQuantity)
                    .build();
            return book;
        }

        private static Member createMember(String name, String city, String street, String zipcode) {
            Member member = Member.builder()
                    .name(name)
                    .address(new Address(city, street, zipcode)).build();
            return member;
        }
    }
}


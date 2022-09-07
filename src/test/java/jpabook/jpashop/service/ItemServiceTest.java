package jpabook.jpashop.service;

import jpabook.jpashop.domain.exception.NotEnoughStockException;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.domain.repository.ItemRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.assertEquals;


@SpringBootTest
@RunWith(SpringRunner.class)
@Transactional
public class ItemServiceTest {

    @Autowired
    ItemService itemService;

    @Autowired
    ItemRepository itemRepository;

    @Test
    public void 아이템_등록() {
        //given
        Item item = Book.builder().name("book1").build();

        //when
        Long itemId = itemService.saveItem(item);

        //then
        assertEquals(item, itemRepository.findOne(itemId));
    }

    @Test(expected = NotEnoughStockException.class)
    public void 아이템_재고() {
        //given
        Item item = Book.builder().name("book1").stockQuantity(0).build();
        //when
        item.removeStock(1);
        //then
        //NotEnoughStockException
    }
    

}
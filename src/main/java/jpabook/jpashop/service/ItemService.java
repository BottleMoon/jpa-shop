package jpabook.jpashop.service;

import jpabook.jpashop.controller.BookForm;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.ItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ItemService {

    public final ItemRepository itemRepository;

    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @Transactional
    public Long saveItem(Item item) {
        itemRepository.save(item);
        return item.getId();
    }

    //변경감지로 업데이트.
    @Transactional
    public Long updateItem(Long itemId, BookForm form) {

        Item findItem = itemRepository.findOne(itemId);

        //update 전용 메서드를 만들어서 setter 사용을 지양함.
        findItem.updateItem(form.getName(), form.getPrice(), form.getStockQuantity());
        return findItem.getId();
    }

    public Item findOne(Long id) {
        return itemRepository.findOne(id);
    }

    public List<Item> findAll() {
        return itemRepository.findItems();
    }

}

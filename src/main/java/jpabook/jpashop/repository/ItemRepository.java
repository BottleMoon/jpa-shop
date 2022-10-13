package jpabook.jpashop.repository;

import jpabook.jpashop.domain.item.Item;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
public class ItemRepository {

    private final EntityManager em;

    public ItemRepository(EntityManager em) {
        this.em = em;
    }

    public void save(Item item) {
        em.persist(item);
    }

    public Item findOne(Long id) {
        return em.find(Item.class, id);
    }

    public List<Item> findItems() {
        return em.createQuery("select i from Item i", Item.class).getResultList();
    }
}

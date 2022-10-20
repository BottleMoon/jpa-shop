package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.OrderSimpleQueryDto;
import lombok.Data;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * XToOne
 * Order
 * Order -> Member
 * Order -> Delivery
 */
@RestController
public class OrderSimpleApiController {

    private final OrderRepository orderRepository;

    public OrderSimpleApiController(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    // 엔티티를 노출 시키면 양방향 연관관계가 있을 때 조회 시 무한루프에 빠지게 된다.
    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        return all;
    }

    //엔티티를 DTO로 변환
    //N+1의 문제가 남아있다.
    @GetMapping("/api/v2/simple-orders")
    public List<SimpleOrderDto> ordersV2() {
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());

        return orders.stream()
                .map(SimpleOrderDto::new)
                .collect(Collectors.toList());
    }

    //fetch join을 이용한 성능 최적화
    //fetch join으로 인해 member와 delivery가 한번에 조회되기 때문에 N+1이 해결된다.
    @GetMapping("/api/v3/simple-orders")
    public List<SimpleOrderDto> ordersV3() {
        List<Order> orders = orderRepository.findAllWithMemberDelivery();
        return orders.stream().map(SimpleOrderDto::new).collect(Collectors.toList());
    }

    @GetMapping("/api/v4/simple-orders")
    public List<OrderSimpleQueryDto> ordersV4() {
        return orderRepository.findOrderDtos();
    }

    @Data
    static class SimpleOrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;

        public SimpleOrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName(); //LAZY 초기화
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress(); //LAZY 초기화
        }
    }


}

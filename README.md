# 스프링 부트와 JPA 활용1

인프런 김영한님의 실전! 스프링 부트와 JPA 활용1을 들으며 따라 만드는 프로젝트입니다.

## 환경

- spring boot 2.7.3
- jdk 11
- intellij
- Dependencies
    - Spring Web
    - Thymeleaf
    - Spring Data JPA
    - H2 Database
    - Lombok
        - Lombok을 깔면 Preferences - Build, Execution, Deployment - Compiler - Annotation Processors - Enable annotation
          procession에 체크를 해줘야 한다.

## 도메인 분석 설계

### 요구사항 분석

실제 동작하는 화면을 먼저 확인한다.

기능 목록

- **회원 기능**
    - 회원 등록
    - 회원 조회
- **상품 기능**
    - 상품 등록
    - 상품 수정
    - 상품 조회
- **주문 기능**
    - 상품 주문
    - 주문 내역 조회
    - 주문 취소
- **기타 요구사항**
    - 상품은 재고 관리가 필요하다.
    - 상품의 종류는 도서, 음반, 영화가 있다.
    - 상품을 카테고리로 구분할 수 있다.
    - 상품 주문시 배송 정보를 입력할 수 있다.

### 도메인 모델과 테이블 설계

![](readmepic/2.png)
회원, 주문, 상품의 관계: 회원은 여러 상품을 주문할 수 있다. 그리고 한 번 주문할 때 여러 상품을 선택할 수
있으므로 주문과 상품은 다대다 관계다. 하지만 이런 다대다 관계는 관계형 데이터베이스는 물론이고 엔티
티에서도 거의 사용하지 않는다. 따라서 그림처럼 주문상품이라는 엔티티를 추가해서 다대다 관계를 일대
다, 다대일 관계로 풀어냈다.

상품 분류: 상품은 도서, 음반, 영화로 구분되는데 상품이라는 공통 속성을 사용하므로 상속 구조로 표현했
다.

### 회원 엔티티 분석

![](readmepic/3.png)

- 회원(Member): 이름과 임베디드 타입인 주소( Address ), 그리고 주문( orders ) 리스트를 가진다.
- 주문(Order): 한 번 주문시 여러 상품을 주문할 수 있으므로 주문과 주문상품( OrderItem )은 일대다 관계
  다. 주문은 상품을 주문한 회원과 배송 정보, 주문 날짜, 주문 상태( status )를 가지고 있다. 주문 상태는 열
  거형을 사용했는데 주문( ORDER ), 취소( CANCEL )을 표현할 수 있다.
- 주문상품(OrderItem): 주문한 상품 정보와 주문 금액( orderPrice ), 주문 수량( count ) 정보를 가지고
  있다. (보통 OrderLine , LineItem 으로 많이 표현한다.)
- 상품(Item): 이름, 가격, 재고수량( stockQuantity )을 가지고 있다. 상품을 주문하면 재고수량이 줄어든
  다. 상품의 종류로는 도서, 음반, 영화가 있는데 각각은 사용하는 속성이 조금씩 다르다.
- 배송(Delivery): 주문시 하나의 배송 정보를 생성한다. 주문과 배송은 일대일 관계다.
- 카테고리(Category): 상품과 다대다 관계를 맺는다. parent , child 로 부모, 자식 카테고리를 연결한
  다.
- 주소(Address): 값 타입(임베디드 타입)이다. 회원과 배송(Delivery)에서 사용한다.

> 참고: 회원 엔티티 분석 그림에서 Order와 Delivery가 단방향 관계로 잘못 그려져 있다. 양방향 관계가 맞
> 다.

> 참고: 회원이 주문을 하기 때문에, 회원이 주문리스트를 가지는 것은 얼핏 보면 잘 설계한 것 같지만, 객체 세
> 상은 실제 세계와는 다르다. 실무에서는 회원이 주문을 참조하지 않고, 주문이 회원을 참조하는 것으로 충분
> 하다. 여기서는 일대다, 다대일의 양방향 연관관계를 설명하기 위해서 추가했다.

### 회원 테이블 분석

![](readmepic/1.png)

- MEMBER: 회원 엔티티의 Address 임베디드 타입 정보가 회원 테이블에 그대로 들어갔다. 이것은
  DELIVERY 테이블도 마찬가지다.
- ITEM: 앨범, 도서, 영화 타입을 통합해서 하나의 테이블로 만들었다. DTYPE 컬럼으로 타입을 구분한다.

> 참고: 테이블명이 ORDER 가 아니라 ORDERS 인 것은 데이터베이스가 order by 때문에 예약어로 잡고 있
> 는 경우가 많다. 그래서 관례상 ORDERS 를 많이 사용한다.

> 참고: 실제 코드에서는 DB에 소문자 + _(언더스코어) 스타일을 사용하겠다.
> 데이터베이스 테이블명, 컬럼명에 대한 관례는 회사마다 다르다. 보통은 대문자 + _(언더스코어)나 소문자 +  _(언더스코어) 방식 중에 하나를 지정해서 일관성 있게 사용한다. 강의에서 설명할 때는 객체와 차이를
> 나타내기 위해 데이터베이스 테이블, 컬럼명은 대문자를 사용했지만, 실제 코드에서는 소문자 + _(언더스코어) 스타일을 사용하겠다.

### 연관관계 매핑 분석

- 회원과 주문: 일대다 , 다대일의 양방향 관계다. 따라서 연관관계의 주인을 정해야 하는데, 외래 키가 있는 주
  문을 연관관계의 주인으로 정하는 것이 좋다. 그러므로 Order.member 를 ORDERS.MEMBER_ID 외래 키와 매핑한다.
- 주문상품과 주문: 다대일 양방향 관계다. 외래 키가 주문상품에 있으므로 주문상품이 연관관계의 주인이다.
  그러므로 OrderItem.order 를 ORDER_ITEM.ORDER_ID 외래 키와 매핑한다.
- 주문상품과 상품: 다대일 단방향 관계다. OrderItem.item 을 ORDER_ITEM.ITEM_ID 외래 키와 매핑한
  다.
- 주문과 배송: 일대일 양방향 관계다. Order.delivery 를 ORDERS.DELIVERY_ID 외래 키와 매핑한다.
- 카테고리와 상품: 강의에선 간단하게 @ManyToMany 를 사용해서 매핑했지만. 이 프로젝트는 실무에 가깝게 하기 위해 CategoryItem 엔티티를 따로 생성해서 ManyToOne, OneToMany 로
  엮었다.

> **참고: 외래 키가 있는 곳을 연관관계의 주인으로 정해라.** 연관관계의 주인은 단순히 외래 키를 누가 관리하냐의 문제이지 비즈니스상 우위에 있다고 주인으로 정하면
> 안된다.. 예를 들어서 자동차와 바퀴가 있으면, 일대다 관계에서 항상 다쪽에 외래 키가 있으므로 외래 키가
> 있는 바퀴를 연관관계의 주인으로 정하면 된다. 물론 자동차를 연관관계의 주인으로 정하는 것이 불가능 한
> 것은 아니지만, 자동차를 연관관계의 주인으로 정하면 자동차가 관리하지 않는 바퀴 테이블의 외래 키 값이
> 업데이트 되므로 관리와 유지보수가 어렵고, 추가적으로 별도의 업데이트 쿼리가 발생하는 성능 문제도 있
> 다.

## 엔티티 설계시 주의점

### 엔티티는 가급적 Setter 를 사용하지 말자

- setter가 모드 열려있으면 변경 포인트가 너무 많아서 유지보수가 너무 어렵다
- 강의에선 편의를 위해 모든 setter를 열어뒀지만 이 프로젝트에서는 가급적이면 setter를 열어두지 않았다.

### 모든 연관관계는 지연로딩으로 설정

- 즉시로딩(EAGER)는 예측이 어렵고, 어떤 SQL이 실행될지 추측하기 어렵다. 특히 JPQL을 실행할 때 N + 1 문제가 자주 발생한다.
- 실무에서 모든 연관관계는 지연로딩(LAZY)으로 설정해야 한다.
- 연관된 엔티티를 함께 DB에서 조회해야 한다면 fetch join 또는 엔티티 그래프 기능을 사용한다.
- @XxxToOne 관계는 기본이 즉시로딩이므로 직접 지연로딩으로 설정해야 한다.

### 컬렉션은 필드에서 초기화하자

- null 문제에서 안전하다
- 하이버네이트는 엔티티를 영속화 할 때 컬랙션을 감싸서 하이버네이트가 제공하는 내장 컬렉션으로 변경한다. 만약 임의의 메서드에서 컬렉션을 잘못생성하면 하이버네이트 내부 메커니즘에 문제가 발생할 수 있다. 따라서
  필드레벨에서 생성하는 것이 가장 안전하고 코드도 간결하다.

### 기본 생성자를 protected로 설정하자.

- 기본 생성자로 객체를 생성하고 setter등으로 채워넣으면 유지보수가 힘들어진다.
    - 기본 생성자를 protected로 설정하고 객체 생성 방법을 통일해서 유지보수하기 쉽게 만들자.

## 주문

> 주문 서비스의 주문과 주문 취소 메서드를 보면 비즈니스 로직 대부분이 엔티티에 있다. 서비스 계층 은 단순히 엔티티에 필요한 요청을 위임하는 역할을 한다. 이처럼 엔티티가 비즈니스 로직을 가지고 객체 지
> 향의 특성을 적극 활용하는 것을 **도메인 모델 패턴**(http://martinfowler.com/eaaCatalog/domainModel.html)이라 한다. 반대로 엔티티에는 비즈니스 로직이 거의 없고
> 서비스
> 계층에서 대부분 의 비즈니스 로직을 처리하는 것을 **트랜잭션 스크립트 패턴**(http://martinfowler.com/eaaCatalog/transactionScript.html)이라 한다.

## 동적 쿼리

- jpql 으로 if문을 사용하여 문자열을 합성해서 동적쿼리
    - 가독성이 떨어지고 구현이 복잡하고 실수로 인한 버그 발생 가능성이 매우 높음
- Criteria 사용
    - JPA 표준스팩이고 jpql을 사용하는 것 보단 낫지만, 유지보수성이 힘들고 가독성이 떨어짐.
- Querydsl 사용

## 변경 감지와 병합

### 변경 감지 (dirty checking)

- 영속성 컨텍스트에서 엔티티를 조회한 후에 영속상태인 엔티티를 수정.

### 병합 (merge)

- em.merge()
- 준영속 엔티티의 식별자 값으로 영속 엔티티를 조회.
- 영속 엔티티의 값을 준영속 엔티티의 값으로 모두 교체한다.

> **주의**: 변경 감지 기능을 사용하면 원하는 속성만 선택해서 변경할 수 있지만, 병합을 사용하면 모든 속성이 변경된다.
> 병합시 값이 없으면 null로 업데이트 할 위험도 있다. (merge 는 모든 필드를 교체한다.)
> **엔티티를 변경할 떄는 항상 변경 감지(dirty checking) 을 사용하자.**

## 메모

@PersistenceContext

- 다른 설정 필요 없이 EntityManager 를 주입해줌.

@Transactional

- spring 라이브러리를 사용하는 것을 권장.
- @Transactional 이 붙은 클래스와 메서드는 트랜잭션 처리가 된다.
- test에 @Transactional 어노테이션이 붙으면 테스트가 끝나고 롤백을 한다.
- 롤백을 하기 싫으면 @Rollback(false) 를 달면 된다.

### Service단에서 id를 넘겨받아 조회하는 이유

- controller단에서 entity를 조회해서 service단으로 넘기면 Service단의 Transaction 바깥에서 조회했기 때문에 영속 상태가 애매해진다.
    - controller에선 엔티티를 넘겨주지 말고 id정도만 넘겨주고 Service의 Transaction 안에서 비지니스 로직을 처리하는 것이 좋다.

### 테스트

테스트 패키지에 resources 폴더를 만들어 설정 파일을 생성해 테스트의 설정 파일을 따로 만드는게 좋다.

- 테스트 DB는 인메모리를 사용하자

# 스프링 부트와 JPA 활용2

## API 개발 기본

- 엔티티를 외부로 노출하지 마라 (DTO를 써야 함.)
    - 실무에서 엔티티나 API 스펙이 중간에 수정될 수도 있다. 그런데 엔티티와 API스펙이 1대1로 매핑되어 버리면 엔티티나 API 스펙이 변경될 때 문제가 생길 가능성이 크다. API 스펙을 위한 별도의
      클래스를 만들어야 한다. 그게 DTO
    - 한 엔티티에 여러가지 API 스펙이 있을 수 있는데 그럴 땐 각각의 API스펙에 맞춰서 DTO를 생성하면 된다.
    - API스펙에 맞게 만든 클래스이기 때문에 API스펙 문서를 보지 않아도 어떤 필드가 넘어 오는지 알 수 있다.
- collection 을 그대로 보내면 배열로 json이 보내져서 유연성이 떨어진다.
    - 제네릭 클래스를 생성한 뒤 한번 감싸서 보내면 됨 다른 정보를 추가할 수 있다
    - ex)

  ```java
  @GetMapping("/api/v2/members")
  public Result membersV2() {
      List<Member> findMembers = memberService.findMembers();
      List<MemberDto> collect = findMembers.stream().map(m -> new MemberDto(m.getName()))
              .collect(Collectors.toList());
      return new Result(collect.size(), collect);
  }
      
  //이렇게 한번 감싸주는 이유
  //collection으로 바로 내면 json이 배열타입으로 나가기 때문에 유연성이 떨어진다.
  @Data
  @AllArgsConstructor
  static class Result<T> {
      private int count;
      private T data;
  }
  ```

## API 개발 고급

### 지연 로딩과 조회 성능 최적화

**v1. 엔티티 직접 노출**

OrderSimpleController.java

```sql
@
GetMapping
("/api/v1/simple-orders")
public List<Order> ordersV1() {
    List<Order> all = orderRepository.findAllByString(new OrderSearch());
    return
all;
}
```

**v2. 엔티티를 DTO로 변환**

OrderSimpleApiRepository.java

```java
//엔티티를 DTO로 변환
@GetMapping("/api/v2/simple-orders")
publicList<SimpleOrderDto> ordersV2(){
        List<Order> orders=orderRepository.findAllByString(new OrderSearch());

        List<SimpleOrderDto> result=orders.stream()
        .map(SimpleOrderDto::new)
        .collect(Collectors.toList());
        return result;
        }

@Data
static class SimpleOrderDto {
    private Long orderId;
    private String name;
    private LocalDateTime orderDate;
    privateOrderStatusorderStatus;
    private Address address;

    public SimpleOrderDto(Orderorder) {
        orderId = order.getId();
        name = order.getMember().getName();//LAZY 초기화
        orderDate = order.getOrderDate();
        orderStatus = order.getStatus();
        address = order.getDelivery().getAddress();//LAZY 초기화
    }
}
```

**v3. fetch join으로 최적화**

- fetch join은 LAZY를 무시하고 PROXY 객체가 아닌 member와 delivery의 값을 다 채워서 한번에 가져온다
- fetch join으로 member와 delivery가 이미 영속화 되어있기 때문에 지연로딩이 일어나지 않는다.
- 따라서 쿼리 한번으로 조회되기 때문에 N+1이 해결된다.

OrderSimpleApiController.java

```java
@GetMapping("/api/v3/simple-orders")
public List<SimpleOrderDto> ordersV3(){
        List<Order> orders=orderRepository.findAllWithMemberDelivery();
        return orders.stream().map(Simple OrderDto::new).collect(Collectors.toList());
        }
```

OrderRepository.java

```java
public List<Order> findAllWithMemberDelivery(){
        return em.createQuery(
        "select o from Order o"+
        " join fetch o.member m"+
        " join fetch o.delivery d",Order.class
    ).getResultList();
            }
```

**v4. JPA에서 DTO로 바로 조회**

OrderSimpleApiController.java

```java
@GetMapping("/api/v4/simple-orders")
public List<OrderSimpleQueryDto> ordersV4(){
        return orderRepository.findOrderDtos();
        }
```

OrderRepository.java

```java
public List<OrderSimpleQueryDto> findOrderDtos(){
        return em.createQuery(
        "select new jpabook.jpashop.repository.OrderSimpleQueryDto(o.id, m.name, o.orderDate, o.status, d.address)"+
        " from Order o"+
        " join o.member m"+
        " join o.delivery d",OrderSimpleQueryDto.class)
        .getResultList();

        }
```

OrderSimpleQueryDto.java

```java

@Data
public class OrderSimpleQueryDto {
    private Long orderId;
    private String name;
    private LocalDateTime orderDate;
    private OrderStatus orderStatus;
    private Address address;

    public OrderSimpleQueryDto(Long orderId, String name, LocalDateTime orderDate, OrderStatus orderStatus, Address address) {
        this.orderId = orderId;
        this.name = name;
        this.orderDate = orderDate;
        this.orderStatus = orderStatus;
        this.address = address;
    }
}
```

v3의 쿼리

```sql
select order0_.order_id       as order_id1_6_0_,
       member1_.member_id     as member_i1_4_1_,
       delivery2_.delivery_id as delivery1_2_2_,
       order0_.delivery_id    as delivery4_6_0_,
       order0_.member_id      as member_i5_6_0_,
       order0_.order_date     as order_da2_6_0_,
       order0_.status         as status3_6_0_,
       member1_.city          as city2_4_1_,
       member1_.street        as street3_4_1_,
       member1_.zipcode       as zipcode4_4_1_,
       member1_.name          as name5_4_1_,
       delivery2_.city        as city2_2_2_,
       delivery2_.street      as street3_2_2_,
       delivery2_.zipcode     as zipcode4_2_2_,
       delivery2_.status      as status5_2_2_
from orders order0_
         inner join
     member member1_ on order0_.member_id = member1_.member_id
         inner join
     delivery delivery2_ on order0_.delivery_id = delivery2_.delivery_id
```

v4의 쿼리

```sql
select order0_.order_id   as col_0_0_,
       member1_.name      as col_1_0_,
       order0_.order_date as col_2_0_,
       order0_.status     as col_3_0_,
       delivery2_.city    as col_4_0_,
       delivery2_.street  as col_4_1_,
       delivery2_.zipcode as col_4_2_
from orders order0_
         inner join
     member member1_ on order0_.member_id = member1_.member_id
         inner join
     delivery delivery2_ on order0_.delivery_id = delivery2_.delivery_id
```

**v3의 장점**

- v2보단 훨씬 성능 최적화가 된다. (N+1 문제 해결)
- 코드를 짜기 쉽다.
- DTO를 바꿔도 재사용이 용이하다.

**v4의 장점**

- v3보다 조금 더 최적화가 된다.(애플리케이션 네트워크 용량 최적화)

**v4의 단점**

- 하나의 DTO에 맞춰서 쿼리를 짜기 때문에 repository 재사용성이 떨어짐.
- repository에 API 스펙이 들어가버린다. (계층이 깨져버림)
    - 쿼리 전용 repository를 만들어서 계층을 나누는 방법이 있다.
- 코드가 지저분해진다.

> 어떤 방식이 더 좋다고 할 순 없다고 한다. 상황에 따라 선택하면 된다.


> **쿼리 방식 선택 권장 순서**
> 1. 우선 엔티티를 DTO로 변환하는 방법을 선택한다.
> 2. 필요하면 fetch join으로 성능을 최적화한다. → 대부분의 성능 이슈가 해결된다.
> 3. 그래도 안되면 DTO로 직접 조회하는 방법을 사용한다.
> 4. 최후의 방법은 JPA가 제공하는 네이티브 SQL이나 스프링 JDBC Template를 사용해서 SQL을 직접 사용한다.

### 컬렉션 조회 최적화

주문내역에서 추가로 주문한 상품 정보를 추가로 조회하자.

Order 기준으로 컬렉션인 OrderItem와 Item이 필요하다

앞의 예제에서는 toOne(OneToOne, ManyToOne) 관계만 있었다. 이번에는 컬렉션인 일대다 관계(OneToMany)를 조회하고, 최적화하는 방법을 알아보자.

**v1. 엔티티 직접 노출**

OrderApiController.java

```java
@GetMapping("/api/v1/orders")
public List<Order> ordersV1(){
        List<Order> all=orderRepository.findAllByString(new OrderSearch());
        for(Order order:all){
        order.getMember().getName();
        order.getDelivery().getAddress();
        List<OrderItem> orderItems=order.getOrderItems();
        orderItems.stream().forEach(o->o.getItem().getName());
        }
        return all;
        }
```

**v2. 엔티티를 DTO로 변환**

OrderApiController.java

```java
@GetMapping("/api/v2/orders")
public List<OrderDto> ordersV2(){
        List<Order> orders=orderRepository.findAllByString(new OrderSearch());
        return orders.stream()
        .map(OrderDto::new)
        .collect(Collectors.toList());
        }
```

- 지연 로딩으로 너무 많은 SQL 실행

**v3. 엔티티를 DTO로 변환 - fetch join 최적화**

OrderApiController.java

```java
@GetMapping("/api/v3/orders")
public List<OrderDto> ordersV3(){
        List<Order> orders=orderRepository.findAllWithItem();
        return orders.stream()
        .map(OrderDto::new)
        .collect(Collectors.toList());
        }
```

OrderRepository.java

```java
public List<Order> findAllWithItem(){
        return em.createQuery(
        "select distinct o from Order o"+
        " join fetch o.member"+
        " join fetch o.delivery"+
        " join fetch o.orderItems oi"+
        " join fetch oi.item i",Order.class).getResultList();

        }
```

- fetch join으로 SQL이 1번만 실행됨
- distinct를 사용해서 중복을 걸러준다.
- 단점
    - 페이징 불가능
    - 데이터 중복으로 인한 네트워크 용량 이슈

> 참고: 컬렉션 페치 조인을 사용하면 페이징이 불가능하다. 하이버네이트는 경고 로그를 남기면서 모든
> 데이터를 DB에서 읽어오고, 메모리에서 페이징 해버린다(매우 위험하다). 자세한 내용은 자바 ORM 표준
> JPA 프로그래밍의 페치 조인 부분을 참고하자.

> 참고: 컬렉션 페치 조인은 1개만 사용할 수 있다. 컬렉션 둘 이상에 페치 조인을 사용하면 안된다. 데이터가
> 부정합하게 조회될 수 있다. 자세한 내용은 자바 ORM 표준 JPA 프로그래밍을 참고하자.

**v3.1. 페이징의 한계 돌파**
application.yml

```
jpa:
    hibernate:
      ddl-auto: create
    properties:
      hibernate:
        format_sql: true
        default_batch_fetch_size: 100
```

OrderApiController.java

```java
@GetMapping("/api/v3.1/orders")
public List<OrderDto> ordersV3_page(
@RequestParam(value = "offset", defaultValue = "0") int offset,
@RequestParam(value = "offset", defaultValue = "100") int limit){
        List<Order> orders=orderRepository.findAllWithMemberDelivery(offset,limit);
        return orders.stream()
        .map(OrderDto::new)
        .collect(Collectors.toList());
        }
```

OrderRepository.java

```java
public List<Order> findAllWithMemberDelivery(int offset,int limit){
        return em.createQuery(
        "select o from Order o"+
        " join fetch o.member m"+
        " join fetch o.delivery d",Order.class
            )
                    .setFirstResult(offset)
                    .setMaxResults(limit)
                    .getResultList();
                    }
```

- 장점
    - 쿼리 호출 수가 1 + N 에서 1 + 1으로 최적화된다.
    - join보다 DB 데이터 전송량이 최적화 된다.(DB에서 부터 중복이 없음)
    - fetch join 방식보다 쿼리 호출 수가 약간 증가하지만, DB 데이터 전송량은 감소된다
    - 페이징이 가능하다.
- 결론
    - ToOne 관계는 중복이 되지 않으니 fetch join을 해도 페이징에 영향을 주지 않는다. 따라서 ToOne 관계는 fetch join으로 최적화 하고 나머지는 **
      hibernate.fefault_batch_fetch_size**로 설정으로 최적화하자.

> 참고: default_batch_fetch_size 의 크기는 적당한 사이즈를 골라야 하는데, 100~1000 사이를 선택하는 것을 권장한다. 이 전략을 SQL IN 절을 사용하는데, 데이터베이스에 따라 IN 절
> 파라미터를 1000으로 제한하기도 한다. 1000으로 잡으면 한번에 1000개를 DB에서 애플리케이션에 불러오므로 DB 에 순간 부하가 증가할 수 있다. 하지만 애플리케이션은 100이든 1000이든 결국 전체
> 데이터를 로딩해야 하므로 메모리 사용량이 같다. 1000으로 설정하는 것이 성능상 가장 좋지만, 결국 DB든 애플리케이션이든 순간 부하를 어디까지 견딜 수 있는지로 결정하면 된다.

v4: DTO 직접 조회

OrderApiController.java

```java
@GetMapping("/api/v4/orders")
public List<OrderQueryDto> ordersV4(){
        return orderQueryRepository.findOrderQueryDtos();
        }
```

OrderQueryRepository.java

```java

@Repository
public class OrderQueryRepository {

    private final EntityManager em;

    public OrderQueryRepository(EntityManager em) {
        this.em = em;
    }

    /**
     * 컬렉션은 별도로 조회
     * Query: 루트 1번, 컬렉션 N 번 * 단건 조회에서 많이 사용하는 방식
     */
    public List<OrderQueryDto> findOrderQueryDto() {
        //루트 조회(toOne 코드를 모두 한번에 조회)
        List<OrderQueryDto> result = findOrders();
        //루프를 돌면서 컬렉션 추가(추가 쿼리 실행)
        result.forEach(o -> {
            List<OrderItemQueryDto> orderItems = findOrderItems(o.getOrderId());
            o.setOrderItems(orderItems);
        });
        return result;
    }

    /**
     * 1:N 관계인 orderItems 조회
     */
    private List<OrderItemQueryDto> findOrderItems(Long orderId) {
        return em.createQuery(
                        "select new jpabook.jpashop.repository.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count)" +
                                " from OrderItem oi" +
                                " join oi.item i" +
                                " where oi.order.id = :orderId", OrderItemQueryDto.class)
                .setParameter("orderId", orderId)
                .getResultList();

    }

    /**
     * 1:N 관계(컬렉션)를 제외한 나머지를 한번에 조회
     */
    private List<OrderQueryDto> findOrders() {
        return em.createQuery("select new jpabook.jpashop.repository.query.OrderQueryDto(o.id,m.name,o.orderDate,o.status,d.address)" +
                        " from Order o" +
                        " join o.member m" +
                        " join o.delivery d", OrderQueryDto.class)
                .getResultList();
    }
}
```

OrderItemQueryDto.java

```java

@Data
@AllArgsConstructor
public class OrderItemQueryDto {

    private Long orderId;
    private String itemName;
    private int orderPrice;
    private int count;
}
```

OrderQueryDto.java

```java

@Data
public class OrderQueryDto {

    private Long orderId;
    private String name;
    private LocalDateTime orderDate;
    private OrderStatus orderStatus;
    private Address address;
    private List<OrderItemQueryDto> orderItems;

    public OrderQueryDto(Long orderId, String name, LocalDateTime orderDate, OrderStatus orderStatus, Address address) {
        this.orderId = orderId;
        this.name = name;
        this.orderDate = orderDate;
        this.orderStatus = orderStatus;
        this.address = address;
    }
}
```

- Query: 루트 1번, 컬렉션 N 번 실행
- ToOne(N:1, 1:1) 관계들을 먼저 조회하고, ToMany(1:N) 관계는 각각 별도로 처리한다.
    - 이런 방식을 선택한 이유는 다음과 같다.
    - ToOne 관계는 조인해도 데이터 row 수가 증가하지 않는다.
    - ToMany(1:N) 관계는 조인하면 row 수가 증가한다.
- row 수가 증가하지 않는 ToOne 관계는 조인으로 최적화 하기 쉬우므로 한번에 조회하고, ToMany 관계는 최적화 하기 어려우므로 findOrderItems() 같은 별도의 메서드로 조회한다.

**v5: DTO직접 조회 - 컬렉션 조회 최적화**

OrderApiController.java

```java
@GetMapping("/api/v5/orders")
public List<OrderQueryDto> order_v5(){
        return orderQueryRepository.findAllByDto_optimization();
        }
```

OrderQueryRepository.java

```java
public List<OrderQueryDto> findAllByDto_optimization(){
        List<OrderQueryDto> result=findOrders();

        Map<Long, List<OrderItemQueryDto>>orderItemMap=findOrderItemMap(toOrderIds(result));

        result.forEach(o->o.setOrderItems(orderItemMap.get(o.getOrderId())));

        return result;
        }

private List<OrderQueryDto> findOrders(){
        return em.createQuery("select new jpabook.jpashop.repository.query.OrderQueryDto(o.id,m.name,o.orderDate,o.status,d.address)"+
        " from Order o"+
        " join o.member m"+
        " join o.delivery d",OrderQueryDto.class)
        .getResultList();
        }

private Map<Long, List<OrderItemQueryDto>>findOrderItemMap(List<Long> orderIds){
        List<OrderItemQueryDto> orderItems=em.createQuery(
        "select new jpabook.jpashop.repository.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count)"+
        " from OrderItem oi"+
        " join oi.item i"+
        " where oi.order.id in :orderIds",OrderItemQueryDto.class)
        .setParameter("orderIds",orderIds)
        .getResultList();

        return orderItems.stream()
        .collect(Collectors.groupingBy(OrderItemQueryDto::getOrderId));
        }
```

- Query: 루트 1번, 컬렉션 1번
- ToOne 관계들을 먼저 조회하고, 여기서 얻은 식별자 orderId로 ToMany 관계인 OrderItem을 한꺼번에 조회
- MAP을 사용해서 매칭 성능 향상(O(1))

v6: DTO 직접 조회 - 플랫 데이터 최적화

OrderApiController.java

```java
@GetMapping("/api/v6/orders")
public List<OrderQueryDto> order_v6(){
        List<OrderFlatDto> flats=orderQueryRepository.findAllByDto_flat();

        return flats.stream()
        .collect(groupingBy(o->new OrderQueryDto(o.getOrderId(),
        o.getName(),o.getOrderDate(),o.getOrderStatus(),o.getAddress()),
        mapping(o->new OrderItemQueryDto(o.getOrderId(),o.getItemName(),o.getOrderPrice(),o.getCount()),
        toList()))).entrySet().stream()
        .map(e->new OrderQueryDto(e.getKey().getOrderId(),e.getKey().getName(),e.getKey().getOrderDate(),
        e.getKey().getOrderStatus(),e.getKey().getAddress(),e.getValue())).collect(toList());
        }
```

OrderQueryRepository.java

```java
public List<OrderFlatDto> findAllByDto_flat(){
        return em.createQuery(
        "select new jpabook.jpashop.repository.query.OrderFlatDto(o.id, m.name, o.orderDate, o.status, d.address, i.name, oi.orderPrice, oi.count)"+
        " from Order o"+
        " join o.member m"+
        " join o.delivery d"+
        " join o.orderItems oi"+
        " join oi.item i",OrderFlatDto.class)
        .getResultList();
        }
```

OrderFlatDto.java

```java

@Data
public class OrderFlatDto {

    private Long orderId;
    private String name;
    private LocalDateTime orderDate;
    private OrderStatus orderStatus;
    private Address address;

    private String itemName;
    private int orderPrice;
    private int count;

    public OrderFlatDto(Long orderId, String name, LocalDateTime orderDate, OrderStatus orderStatus, Address address, String itemName, int orderPrice, int count) {
        this.orderId = orderId;
        this.name = name;
        this.orderDate = orderDate;
        this.orderStatus = orderStatus;
        this.address = address;
        this.itemName = itemName;
        this.orderPrice = orderPrice;
        this.count = count;
    }
}
```

OrderQueryDto.java

```java

@Data
@EqualsAndHashCode(of = "orderId")
public class OrderQueryDto {

    private Long orderId;
    private String name;
    private LocalDateTime orderDate;
    private OrderStatus orderStatus;
    private Address address;
    private List<OrderItemQueryDto> orderItems;

    public OrderQueryDto(Long orderId, String name, LocalDateTime orderDate, OrderStatus orderStatus, Address address) {
        this.orderId = orderId;
        this.name = name;
        this.orderDate = orderDate;
        this.orderStatus = orderStatus;
        this.address = address;
    }

    public OrderQueryDto(Long orderId, String name, LocalDateTime orderDate, OrderStatus orderStatus, Address address, List<OrderItemQueryDto> orderItems) {
        this.orderId = orderId;
        this.name = name;
        this.orderDate = orderDate;
        this.orderStatus = orderStatus;
        this.address = address;
        this.orderItems = orderItems;
    }
}
```

- 장점
    - Query: 1번
- 단점
    - 쿼리는 한번이지만 조인으로 인해 DB에서 애플리케이션에 전달하는 데이터에 중복 데이터가
      추가되므로 상황에 따라 V5 보다 더 느릴 수 도 있다.
    - 애플리케이션에서 추가 작업이 크다.
    - 페이징 불가능

**API 개발 고급 정리**

**정리**

- 엔티티 조회
    - 엔티티를 조회해서 그대로 반환: V1
    - 엔티티 조회 후 DTO로 변환: V2
    - 페치 조인으로 쿼리 수 최적화: V3
    - 컬렉션 페이징과 한계 돌파: V3.1
        - 컬렉션은 페치 조인시 페이징이 불가능
        - ToOne 관계는 페치 조인으로 쿼리 수 최적화
        - 컬렉션은 페치 조인 대신에 지연 로딩을 유지하고, hibernate.default_batch_fetch_size , @BatchSize 로 최적화
- DTO 직접 조회
    - JPA에서 DTO를 직접 조회: V4
    - 컬렉션 조회 최적화 - 일대다 관계인 컬렉션은 IN 절을 활용해서 메모리에 미리 조회해서 최적화: V5
    - 플랫 데이터 최적화 - JOIN 결과를 그대로 조회 후 애플리케이션에서 원하는 모양으로 직접 변환: V6

**권장 순서**

1. 엔티티조회방식으로우선접근
1. 페치조인으로 쿼리 수를 최적화
2. 컬렉션 최적화
    1. 페이징 필요 hibernate.default_batch_fetch_size , @BatchSize 로 최적화
    2. 페이징 필요X 페치 조인 사용
2. 엔티티조회방식으로해결이안되면DTO조회방식사용
3. DTO 조회 방식으로 해결이 안되면 NativeSQL or 스프링 JdbcTemplate

> 참고: 엔티티 조회 방식은 페치 조인이나, hibernate.default_batch_fetch_size , @BatchSize 같이
> 코드를 거의 수정하지 않고, 옵션만 약간 변경해서, 다양한 성능 최적화를 시도할 수 있다. 반면에 DTO를
> 직접 조회하는 방식은 성능을 최적화 하거나 성능 최적화 방식을 변경할 때 많은 코드를 변경해야 한다.

> 참고: 개발자는 성능 최적화와 코드 복잡도 사이에서 줄타기를 해야 한다. 항상 그런 것은 아니지만, 보통 성능 최적화는 단순한 코드를 복잡한 코드로 몰고간다.
> 엔티티 조회 방식은 JPA가 많은 부분을 최적화 해주기 때문에, 단순한 코드를 유지하면서, 성능을 최적화 할 수 있다.
> 반면에 DTO 조회 방식은 SQL을 직접 다루는 것과 유사하기 때문에, 둘 사이에 줄타기를 해야 한다.

**DTO 조회 방식의 선택지**

- DTO로 조회하는 방법도 각각 장단이 있다. V4, V5, V6에서 단순하게 쿼리가 1번 실행된다고 V6이 항상 좋은 방법인 것은 아니다.
- V4는 코드가 단순하다. 특정 주문 한건만 조회하면 이 방식을 사용해도 성능이 잘 나온다. 예를 들어서 조회한 Order 데이터가 1건이면 OrderItem을 찾기 위한 쿼리도 1번만 실행하면 된다.
- V5는 코드가 복잡하다. 여러 주문을 한꺼번에 조회하는 경우에는 V4 대신에 이것을 최적화한 V5 방식을 사용해야 한다. 예를 들어서 조회한 Order 데이터가 1000건인데, V4 방식을 그대로
  사용하면, 쿼리가 총 1 + 1000번 실행된다. 여기서 1은 Order 를 조회한 쿼리고, 1000은 조회된 Order의 row 수다. V5 방식으로 최적화 하면 쿼리가 총 1 + 1번만 실행된다.
  상황에 따라 다르겠지만 운영 환경에서 100배
  이상의 성능 차이가 날 수 있다.
- V6는 완전히 다른 접근방식이다. 쿼리 한번으로 최적화 되어서 상당히 좋아보이지만, Order를 기준으로 페이징이 불가능하다. 실무에서는 이정도 데이터면 수백이나, 수천건 단위로 페이징 처리가 꼭
  필요하므로, 이 경우 선택하기 어려운 방법이다. 그리고 데이터가 많으면 중복 전송이 증가해서 V5와 비교해서 성능 차이도 미비하다.

### OSIV와 성능 최적화

- Open Session In View: 하이버네이트
- Open EntityManager In View: JPA
- 관례상 OSIV 라고 한다.

**OSIV ON**

- spring.jpa.open-in-view : true 기본값

이 기본값을 뿌리면서 애플리케이션 시작 시점에 warn 로그를 남기는 것은 이유가 있다.
OSIV 전략은 트랜잭션 시작처럼 최초 데이터베이스 커넥션 시작 시점부터 API 응답이 끝날 때 까지 영속성 컨텍스트와 데이터베이스 커넥션을 유지한다. 그래서 지금까지 View Template이나 API 컨트롤러에서
지연 로딩이 가능했던 것이다.
지연 로딩은 영속성 컨텍스트가 살아있어야 가능하고, 영속성 컨텍스트는 기본적으로 데이터베이스 커넥션을 유지한다. 이것 자체가 큰 장점이다.

그런데 이 전략은 너무 오랜시간동안 데이터베이스 커넥션 리소스를 사용하기 때문에, 실시간 트래픽이 중요한 애플리케이션에서는 커넥션이 모자랄 수 있다. 이것은 결국 장애로 이어진다.
예를 들어서 컨트롤러에서 외부 API를 호출하면 외부 API 대기 시간 만큼 커넥션 리소스를 반환하지 못하고, 유지해야 한다.

**OSIV OFF**

- spring.jpa.open-in-view: false OSIV 종료

OSIV를 끄면 트랜잭션을 종료할 때 영속성 컨텍스트를 닫고, 데이터베이스 커넥션도 반환한다. 따라서 커넥션 리소스를 낭비하지 않는다.
OSIV를 끄면 모든 지연로딩을 트랜잭션 안에서 처리해야 한다. 따라서 지금까지 작성한 많은 지연 로딩 코드를 트랜잭션 안으로 넣어야 하는 단점이 있다. 그리고 view template에서 지연로딩이 동작하지
않는다. 결론적으로 트랜잭션이 끝나기 전에 지연 로딩을 강제로 호출해 두어야 한다.

**커멘드와 쿼리 분리**

실무에서 **OSIV를 끈 상태**로 복잡성을 관리하는 좋은 방법이 있다. 바로 Command와 Query를 분리하는 것이다.
참고: https://en.wikipedia.org/wiki/Command–query_separation

보통 비즈니스 로직은 특정 엔티티 몇게를 등록하거나 수정하는 것이므로 성능이 크게 문제가 되지 않는다. 그런데 복잡한 화면을 출력하기 위한 쿼리는 화면에 맞추어 성능을 최적화 하는 것이 중요하다. 하지만 그 복잡성에
비해 핵심 비즈니스에 큰 영향을 주는 것은 아니다.
그래서 크고 복잡한 애플리케이션을 개발한다면, 이 둘의 관심사를 명확하게 분리하는 선택은 유지보수 관점에서 충분히 의미 있다.

단순하게 설명해서 다음처럼 분리하는 것이다.

- OrderService
    - OrderService: 핵심 비즈니스 로직
    - OrderQueryService: 화면이나 API에 맞춘 서비스 (주로 읽기 전용 트랜잭션 사용)

보통 서비스 계층에서 트랜잭션을 유지한다. 두 서비스 모두 트랜잭션을 유지하면서 지연 로딩을 사용할 수 있다.

> **참고**: 고객 서비스의 실시간 API는 OSIV를 끄고, ADMIN 처럼 커넥션을 많이 사용하지 않는 곳에서는 OSIV를 켠다.

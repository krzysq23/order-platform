package pl.xsware.orders.infrastructure.persistence.order;

import org.springframework.stereotype.Repository;
import pl.xsware.orders.domain.order.Order;
import pl.xsware.orders.domain.order.OrderId;
import pl.xsware.orders.domain.order.OrderRepository;

import java.util.List;
import java.util.Optional;

@Repository
public class OrderRepositoryImpl implements OrderRepository {

    private final OrderJpaRepository jpaRepository;

    public OrderRepositoryImpl(OrderJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void save(Order order) {
        OrderJpaEntity entity = toEntity(order);
        jpaRepository.save(entity);
    }

    @Override
    public Optional<Order> findById(OrderId orderId) {
        return jpaRepository.findById(orderId.value())
            .map(OrderRepositoryImpl::toDomain);
    }

    @Override
    public List<Order> findByCustomerId(String customerId) {
        return jpaRepository.findAllByCustomerId(customerId)
            .stream()
            .map(OrderRepositoryImpl::toDomain)
            .toList();
    }

    private static OrderJpaEntity toEntity(Order order) {
        return new OrderJpaEntity(
            order.getId().value(),
            order.getCustomerId(),
            order.getStatus(),
            order.getCreatedAt(),
            order.getTotalAmount(),
            order.getCurrency()
        );
    }

    private static Order toDomain(OrderJpaEntity entity) {
        return Order.rehydrate(
            OrderId.of(entity.getId()),
            entity.getCustomerId(),
            entity.getStatus(),
            entity.getCreatedAt(),
            entity.getTotalAmount(),
            entity.getCurrency()
        );
    }
}

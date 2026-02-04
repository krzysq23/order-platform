package pl.xsware.orders.application.order;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.xsware.orders.domain.order.Order;
import pl.xsware.orders.domain.order.OrderRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetOrdersByCustomerIdService
    implements GetOrdersByCustomerIdUseCase {

    private final OrderRepository orderRepository;

    @Override
    public List<Order> getByCustomerId(String customerId) {
        return orderRepository.findByCustomerId(customerId);
    }
}

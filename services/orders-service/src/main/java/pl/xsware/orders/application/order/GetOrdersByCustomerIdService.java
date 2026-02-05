package pl.xsware.orders.application.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.xsware.orders.domain.order.Order;
import pl.xsware.orders.domain.order.OrderRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class GetOrdersByCustomerIdService
    implements GetOrdersByCustomerIdUseCase {

    private final OrderRepository orderRepository;

    @Override
    public List<Order> getByCustomerId(String customerId) {

        log.debug("GET_ORDERS_BY_CUSTOMER_ID customerId={}" ,customerId);
        return orderRepository.findByCustomerId(customerId);
    }
}

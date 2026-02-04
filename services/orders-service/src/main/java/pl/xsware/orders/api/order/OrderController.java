package pl.xsware.orders.api.order;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.xsware.orders.application.order.CreateOrderCommand;
import pl.xsware.orders.application.order.CreateOrderUseCase;
import pl.xsware.orders.application.order.GetOrdersByCustomerIdUseCase;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/orders")
public class OrderController {

    private final CreateOrderUseCase createOrderUseCase;
    private final GetOrdersByCustomerIdUseCase getOrdersByCustomerIdUseCase;

    @GetMapping("/{customerId}")
    public List<OrderResponse> getOrdersByCustomer(@PathVariable String customerId) {
        return getOrdersByCustomerIdUseCase
            .getByCustomerId(customerId)
            .stream()
            .map(order -> new OrderResponse(
                order.getId().value(),
                order.getStatus().name(),
                order.getCreatedAt()
            ))
            .toList();
    }

    @PostMapping
    public ResponseEntity<Void> createOrder(@RequestBody @Valid CreateOrderRequest request) {
        CreateOrderCommand command = new CreateOrderCommand(
            request.getCustomerId()
        );

        createOrderUseCase.create(command);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}

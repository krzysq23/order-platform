package pl.xsware.orders.api.order;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.xsware.orders.application.order.CreateOrderCommand;
import pl.xsware.orders.application.order.CreateOrderUseCase;

@RestController
@AllArgsConstructor
@RequestMapping("/orders")
public class OrderController {

    private final CreateOrderUseCase createOrderUseCase;

    @PostMapping
    public ResponseEntity<Void> createOrder(
        @RequestBody @Valid CreateOrderRequest request
    ) {
        CreateOrderCommand command = new CreateOrderCommand(
            request.getCustomerId()
        );

        createOrderUseCase.create(command);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}

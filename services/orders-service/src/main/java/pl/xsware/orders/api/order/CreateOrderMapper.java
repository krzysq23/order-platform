package pl.xsware.orders.api.order;

import pl.xsware.orders.application.order.CreateOrderCommand;

public final class CreateOrderMapper {

    public static CreateOrderCommand toCommand(CreateOrderRequest request) {
        return new CreateOrderCommand(
            request.getCustomerId(),
            request.getTotalAmount(),
            request.getItems().stream()
                .map(i -> new CreateOrderCommand.Item(i.getSku(), i.getQuantity()))
                .toList()
        );
    }
}

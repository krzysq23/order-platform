package pl.xsware.orders.api.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class CreateOrderRequest {

    @NotBlank
    private String customerId;

    @NotNull
    private BigDecimal totalAmount;

    @NotEmpty
    private List<Item> items;

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    public static class Item {

        @NotBlank
        private String sku;

        @Positive
        private int quantity;
    }
}

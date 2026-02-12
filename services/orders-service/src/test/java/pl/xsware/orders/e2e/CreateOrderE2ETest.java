package pl.xsware.orders.e2e;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import pl.xsware.orders.api.order.CreateOrderRequest;
import pl.xsware.orders.domain.order.Order;
import pl.xsware.orders.domain.order.OrderId;
import pl.xsware.orders.domain.order.OrderRepository;
import pl.xsware.orders.domain.order.OrderStatus;
import pl.xsware.orders.saga.JacksonTestConfig;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.UUID;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(JacksonTestConfig.class)
class CreateOrderE2ETest extends E2ETestBase {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    OrderRepository orderRepository;

    private ObjectMapper objectMapper;

    @Test
    void should_create_order_and_reserve_stock_then_request_payment() throws Exception {

        CreateOrderRequest request = new CreateOrderRequest(
            "customer-123",
            new BigDecimal("1999.99"),
            List.of(new CreateOrderRequest.Item("TV-SAMSUNG-55-QLED", 1))
        );

        MvcResult result = mockMvc.perform(
                post("/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isCreated())
            .andReturn();

        UUID orderId = UUID.fromString(result.getResponse().getContentAsString());

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            Order order = orderRepository
                .findById(OrderId.of(orderId))
                .orElseThrow();

            assertThat(order.getStatus())
                .isEqualTo(OrderStatus.PAYMENT_PENDING);
        });
    }

    @Test
    void should_cancel_order_when_stock_not_available() throws Exception {

        CreateOrderRequest request = new CreateOrderRequest(
            "customer-456",
            new BigDecimal("9999.99"),
            List.of(new CreateOrderRequest.Item("TV-SAMSUNG-55-QLED", 100000))
        );

        MvcResult result = mockMvc.perform(
                post("/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isCreated())
            .andReturn();

        UUID orderId = UUID.fromString(result.getResponse().getContentAsString());

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            Order order = orderRepository
                .findById(OrderId.of(orderId))
                .orElseThrow();

            assertThat(order.getStatus())
                .isEqualTo(OrderStatus.CANCELLED); // albo FAILED, zależnie od Twojej domeny
        });
    }
}

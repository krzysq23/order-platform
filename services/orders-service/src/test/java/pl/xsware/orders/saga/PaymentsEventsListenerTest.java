package pl.xsware.orders.saga;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.xsware.orders.application.event.PaymentCancelledEvent;
import pl.xsware.orders.application.event.PaymentFailedEvent;
import pl.xsware.orders.application.event.PaymentSucceededEvent;
import pl.xsware.orders.application.saga.OrderPaymentSagaService;
import pl.xsware.orders.infrastructure.messaging.kafka.PaymentsEventsListener;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentsEventsListenerTest {

    @Mock
    private OrderPaymentSagaService sagaService;

    private ObjectMapper objectMapper;

    private PaymentsEventsListener listener;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        listener = new PaymentsEventsListener(sagaService, objectMapper);
    }

    @Test
    void onPaymentSucceeded_callsSagaHandle_withSameEvent() {
        // given
        UUID eventId = UUID.fromString("f49dc9ea-488c-4be9-b738-1f248c2a41c3");
        UUID orderId = UUID.fromString("bb609cef-b7bc-4078-996c-95708ede7728");
        UUID paymentId = UUID.fromString("9707cb04-757a-486b-a48d-1d0aa94ec6fe");

        PaymentSucceededEvent event = new PaymentSucceededEvent(
            eventId,
            PaymentSucceededEvent.TYPE,
            PaymentSucceededEvent.VERSION,
            Instant.parse("2026-02-09T18:37:58.673055Z"),
            new PaymentSucceededEvent.Data(
                orderId,
                paymentId,
                "FAKE_PSP",
                "trx_9707cb04-757a-486b-a48d-1d0aa94ec6fe"
            )
        );

        // when
        listener.onPaymentSucceeded(objectMapper.writeValueAsString(event));

        // then
        verify(sagaService, times(1)).handle(event);
        verifyNoMoreInteractions(sagaService);
    }

    @Test
    void onPaymentFailed_callsSagaHandle_withSameEvent() {
        // given
        UUID eventId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();

        PaymentFailedEvent event = new PaymentFailedEvent(
            eventId,
            PaymentFailedEvent.TYPE,
            PaymentFailedEvent.VERSION,
            Instant.now(),
            new PaymentFailedEvent.Data(orderId, paymentId, "DECLINED")
        );

        // when
        listener.onPaymentFailed(objectMapper.writeValueAsString(event));

        // then
        verify(sagaService, times(1)).handle(event);
        verifyNoMoreInteractions(sagaService);
    }

    @Test
    void onPaymentCancelled_callsSagaHandle_withSameEvent() {
        // given
        UUID eventId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();

        PaymentCancelledEvent event = new PaymentCancelledEvent(
            eventId,
            PaymentCancelledEvent.TYPE,
            PaymentCancelledEvent.VERSION,
            Instant.now(),
            new PaymentCancelledEvent.Data(orderId, paymentId, "CANCELLED_BY_USER")
        );

        // when
        listener.onPaymentCancelled(objectMapper.writeValueAsString(event));

        // then
        verify(sagaService, times(1)).handle(event);
        verifyNoMoreInteractions(sagaService);
    }
}

package pl.xsware.orders.application.order;

import java.util.UUID;

public interface OrderPaymentUpdater {

    void markPaid(UUID orderId, UUID paymentId, String provider, String externalId);
    void markPaymentFailed(UUID orderId, String reason);
    void markPaymentCancelled(UUID orderId, String reason);
}

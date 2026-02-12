package pl.xsware.inventory.infrastructure.persistance.view;

import java.util.UUID;

public interface ProductStockView {
    UUID getProductId();
    String getSku();
    String getName();
    String getCategoryCode();

    int getOnHand();
    int getReserved();
    int getAvailable();
}

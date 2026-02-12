package pl.xsware.inventory.application.stock;

import pl.xsware.inventory.domain.stock.StockItem;
import pl.xsware.inventory.domain.stock.vo.Sku;

import java.util.List;

public interface StockItemRepositoryPort {

    List<StockItem> findForUpdateBySkusAndWarehouse(List<Sku> skus, String warehouse);

    List<StockItem> saveAll(List<StockItem> items);
}

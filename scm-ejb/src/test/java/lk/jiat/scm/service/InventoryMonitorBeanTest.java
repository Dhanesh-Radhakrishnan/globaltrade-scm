package lk.jiat.scm.service;

import lk.jiat.scm.exception.InsufficientInventoryException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InventoryMonitorBeanTest {

    private final InventoryMonitorBean bean = new InventoryMonitorBean();

    @Test
    void decrementsQuantityWhenStockIsSufficient() throws InsufficientInventoryException {
        int result = bean.resolveNewQuantity(50, -20, "SKU-001");
        assertEquals(30, result);
    }

    @Test
    void incrementsQuantityOnRestock() throws InsufficientInventoryException {
        int result = bean.resolveNewQuantity(10, 40, "SKU-002");
        assertEquals(50, result);
    }

    @Test
    void allowsExactDepletionToZero() throws InsufficientInventoryException {
        int result = bean.resolveNewQuantity(15, -15, "SKU-003");
        assertEquals(0, result);
    }

    @Test
    void throwsWhenDeltaWouldMakeQuantityNegative() {
        InsufficientInventoryException ex = assertThrows(
                InsufficientInventoryException.class,
                () -> bean.resolveNewQuantity(5, -10, "SKU-004")
        );
        assertEquals("Insufficient stock for item: SKU-004", ex.getMessage());
    }
}
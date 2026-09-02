package lk.jiat.scm.service;

import jakarta.annotation.security.DeclareRoles;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lk.jiat.scm.entity.InventoryItem;
import lk.jiat.scm.exception.InsufficientInventoryException;

import java.util.List;

@Singleton
@DeclareRoles("WarehouseManager")
public class InventoryMonitorBean {

    @EJB
    private AuditService auditService;

    @PersistenceContext(unitName = "SCMPU")
    private EntityManager em;

    @Lock(LockType.READ)
    public InventoryItem checkStock(Long inventoryItemId) {
        InventoryItem item = em.find(InventoryItem.class, inventoryItemId);
        if (item == null) {
            throw new RuntimeException("Inventory item not found: " + inventoryItemId);
        }
        return item;
    }

    @Lock(LockType.WRITE)
    @RolesAllowed({"WarehouseManager", "LogisticsCoordinator"})
    public InventoryItem adjustStock(Long inventoryItemId, int delta) throws InsufficientInventoryException {
        InventoryItem item = em.find(InventoryItem.class, inventoryItemId);
        if (item == null) {
            throw new RuntimeException("Inventory item not found: " + inventoryItemId);
        }
        int newQuantity = resolveNewQuantity(item.getQuantityOnHand(), delta, item.getSku());
        item.setQuantityOnHand(newQuantity);
        return item;
    }

    int resolveNewQuantity(int currentQuantity, int delta, String sku) throws InsufficientInventoryException {
        int newQuantity = currentQuantity + delta;
        if (newQuantity < 0) {
            throw new InsufficientInventoryException("Insufficient stock for item: " + sku);
        }
        return newQuantity;
    }

    @Lock(LockType.READ)
    public List<InventoryItem> findBelowThreshold() {
        return em.createQuery(
                        "SELECT i FROM InventoryItem i WHERE i.quantityOnHand < i.reorderThreshold",
                        InventoryItem.class)
                .getResultList();
    }

    @Lock(LockType.READ)
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    @Schedule(minute = "*/30", hour = "*", persistent = true)
    public void scanAndAlertLowStock() {
        for (InventoryItem item : findBelowThreshold()) {
            auditService.record(
                    "InventoryItem",
                    item.getId(),
                    "LOW_STOCK_ALERT",
                    "SYSTEM_TIMER",
                    "Quantity on hand " + item.getQuantityOnHand() + " below reorder threshold " + item.getReorderThreshold()
            );
            System.out.println("Low stock alert for item: " + item.getSku() + ", quantity on hand: " + item.getQuantityOnHand());
        }
    }
}
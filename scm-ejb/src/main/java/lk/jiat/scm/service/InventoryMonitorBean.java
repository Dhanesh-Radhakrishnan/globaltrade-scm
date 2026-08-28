package lk.jiat.scm.service;

import jakarta.ejb.Lock;
import jakarta.ejb.LockType;
import jakarta.ejb.Singleton;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lk.jiat.scm.entity.InventoryItem;

import java.util.List;

@Singleton
public class InventoryMonitorBean {

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
    public InventoryItem adjustStock(Long inventoryItemId, int delta) {
        InventoryItem item = em.find(InventoryItem.class, inventoryItemId);
        if (item == null) {
            throw new RuntimeException("Inventory item not found: " + inventoryItemId);
        }
        int newQuantity = item.getQuantityOnHand() + delta;
        if (newQuantity < 0) {
            throw new RuntimeException("Insufficient stock for item: " + item.getSku());
        }
        item.setQuantityOnHand(newQuantity);
        return item;
    }

    @Lock(LockType.READ)
    public List<InventoryItem> findBelowThreshold() {
        return em.createQuery(
                        "SELECT i FROM InventoryItem i WHERE i.quantityOnHand < i.reorderThreshold",
                        InventoryItem.class)
                .getResultList();
    }
}
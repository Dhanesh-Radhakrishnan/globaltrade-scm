package lk.jiat.scm.rollback;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lk.jiat.scm.entity.InventoryItem;
import lk.jiat.scm.entity.Order;
import lk.jiat.scm.entity.Vendor;

import java.math.BigDecimal;

@Stateless
public class RollbackTestSupportBean {

    @PersistenceContext(unitName = "SCMPU")
    private EntityManager em;

    public Vendor createTestVendor() {
        Vendor vendor = new Vendor();
        vendor.setVendorName("ROLLBACK_TEST_VENDOR");
        vendor.setCountry("LK");
        em.persist(vendor);
        em.flush();
        return vendor;
    }

    public InventoryItem createTestInventoryItem(int quantityOnHand) {
        InventoryItem item = new InventoryItem();
        item.setSku("ROLLBACK-TEST-SKU-" + System.nanoTime());
        item.setItemName("Rollback Test Item");
        item.setQuantityOnHand(quantityOnHand);
        item.setReorderThreshold(1);
        item.setUnitPrice(BigDecimal.TEN);
        em.persist(item);
        em.flush();
        return item;
    }

    public int getInventoryQuantity(Long inventoryItemId) {
        InventoryItem item = em.find(InventoryItem.class, inventoryItemId);
        return item.getQuantityOnHand();
    }

    public long getOrderCountForVendor(Long vendorId) {
        return em.createQuery("SELECT COUNT(o) FROM Order o WHERE o.vendor.id = :vendorId", Long.class)
                .setParameter("vendorId", vendorId)
                .getSingleResult();
    }

    public void cleanup(Long vendorId, Long inventoryItemId) {
        InventoryItem item = em.find(InventoryItem.class, inventoryItemId);
        if (item != null) {
            em.remove(item);
        }
        Vendor vendor = em.find(Vendor.class, vendorId);
        if (vendor != null) {
            em.remove(vendor);
        }
    }
}
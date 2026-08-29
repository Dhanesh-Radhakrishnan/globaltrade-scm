package lk.jiat.scm.service;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.interceptor.Interceptors;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lk.jiat.scm.entity.InventoryItem;
import lk.jiat.scm.entity.Order;
import lk.jiat.scm.entity.OrderStatus;
import lk.jiat.scm.entity.Shipment;
import lk.jiat.scm.entity.ShipmentStatus;
import lk.jiat.scm.entity.Vendor;
import lk.jiat.scm.interceptor.AuditLoggingInterceptor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Stateless
@Interceptors({AuditLoggingInterceptor.class})
public class OrderProcessingBean {

    @PersistenceContext(unitName = "SCMPU")
    private EntityManager em;

    @EJB
    private VendorServiceBean vendorService;

    @EJB
    private InventoryMonitorBean inventoryMonitor;

    @EJB
    private ShipmentTrackingBean shipmentTrackingBean;

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public Order placeOrder(Long vendorId, Long inventoryItemId, int quantity, LocalDateTime expectedDeliveryDate) {
        Vendor vendor = vendorService.findById(vendorId);
        if (vendor == null) {
            throw new RuntimeException("Vendor not found: " + vendorId);
        }

        InventoryItem item = inventoryMonitor.checkStock(inventoryItemId);
        if (item.getQuantityOnHand() < quantity) {
            throw new RuntimeException("Insufficient stock for item: " + item.getSku());
        }

        BigDecimal totalAmount = item.getUnitPrice().multiply(BigDecimal.valueOf(quantity));

        inventoryMonitor.adjustStock(inventoryItemId, -quantity);

        Shipment shipment = new Shipment();
        shipment.setTrackingNumber("TRK-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase());
        shipment.setShipmentStatus(ShipmentStatus.PENDING);
        em.persist(shipment);
        em.flush();

        shipmentTrackingBean.scheduleDeliveryCheck(shipment.getId(), expectedDeliveryDate);

        Order order = new Order();
        order.setOrderNumber("ORD-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase());
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(totalAmount);
        order.setVendor(vendor);
        order.setInventoryItem(item);
        order.setQuantity(quantity);
        order.setShipment(shipment);
        em.persist(order);
        em.flush();

        return order;
    }

    public Order findById(Long id) {
        return em.find(Order.class, id);
    }
}
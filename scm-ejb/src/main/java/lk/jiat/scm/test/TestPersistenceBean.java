package lk.jiat.scm.test;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import lk.jiat.scm.entity.CustomsDocument;
import lk.jiat.scm.entity.Order;
import lk.jiat.scm.entity.OrderStatus;
import lk.jiat.scm.entity.Shipment;
import lk.jiat.scm.entity.ShipmentStatus;
import lk.jiat.scm.entity.Vendor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Stateless
public class TestPersistenceBean {

    @PersistenceContext(unitName = "SCMPU")
    private EntityManager em;

    public String runThrowawayInsert() {
        Vendor vendor = new Vendor();
        vendor.setVendorName("Test Vendor Co");
        vendor.setContactEmail("vendor@test.com");
        vendor.setContactPhone("0770000000");
        vendor.setCountry("LK");
        vendor.setPerformanceRating(4.5);
        em.persist(vendor);

        Shipment shipment = new Shipment();
        shipment.setTrackingNumber("TRK-TEST-0001");
        shipment.setShipmentStatus(ShipmentStatus.PENDING);
        shipment.setExpectedDeliveryDate(LocalDateTime.now().plusDays(5));
        shipment.setCarrierName("Test Carrier");
        shipment.setOriginCountry("LK");
        shipment.setDestinationCountry("AE");
        em.persist(shipment);

        CustomsDocument doc = new CustomsDocument();
        doc.setDocumentNumber("CUS-TEST-0001");
        doc.setDocumentType("Invoice");
        doc.setComplianceDeadline(LocalDateTime.now().plusDays(3));
        doc.setVerified(false);
        doc.setShipment(shipment);
        em.persist(doc);

        Order order = new Order();
        order.setOrderNumber("ORD-TEST-0001");
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(new BigDecimal("1000.00"));
        order.setVendor(vendor);
        order.setShipment(shipment);
        em.persist(order);

        em.flush();

        return "Persisted vendorId=" + vendor.getId()
                + " shipmentId=" + shipment.getId()
                + " customsDocumentId=" + doc.getId()
                + " orderId=" + order.getId();
    }
}
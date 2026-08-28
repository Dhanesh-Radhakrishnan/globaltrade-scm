package lk.jiat.scm.service;

import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.ejb.Timeout;
import jakarta.ejb.Timer;
import jakarta.ejb.TimerConfig;
import jakarta.ejb.TimerService;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import lk.jiat.scm.entity.Shipment;
import lk.jiat.scm.entity.ShipmentStatus;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Stateless
public class ShipmentTrackingBean {

    @PersistenceContext(unitName = "SCMPU")
    private EntityManager em;

    @EJB
    private AuditService auditService;

    @Resource
    private TimerService timerService;

    public Shipment findById(Long id) {
        return em.find(Shipment.class, id);
    }

    public Shipment findByTrackingNumber(String trackingNumber) {
        try {
            return em.createQuery("SELECT s FROM Shipment s WHERE s.trackingNumber = :trackingNumber", Shipment.class)
                    .setParameter("trackingNumber", trackingNumber)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<Shipment> findByStatus(ShipmentStatus status) {
        return em.createQuery("SELECT s FROM Shipment s WHERE s.shipmentStatus = :status", Shipment.class)
                .setParameter("status", status)
                .getResultList();
    }

    public Shipment updateStatus(Long shipmentId, ShipmentStatus newStatus) {
        Shipment shipment = em.find(Shipment.class, shipmentId);
        if (shipment == null) {
            throw new RuntimeException("Shipment not found: " + shipmentId);
        }
        shipment.setShipmentStatus(newStatus);
        return shipment;
    }

    public Shipment scheduleDeliveryCheck(Long shipmentId, LocalDateTime expectedDeliveryDate) {
        Shipment shipment = em.find(Shipment.class, shipmentId);
        if (shipment == null) {
            throw new RuntimeException("Shipment not found: " + shipmentId);
        }
        shipment.setExpectedDeliveryDate(expectedDeliveryDate);

        Date triggerTime = Date.from(expectedDeliveryDate.atZone(ZoneId.systemDefault()).toInstant());
        TimerConfig config = new TimerConfig();
        config.setInfo(shipmentId);
        config.setPersistent(true);
        timerService.createSingleActionTimer(triggerTime, config);

        return shipment;
    }

    @Timeout
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void handleDeliveryTimeout(Timer timer) {
        Long shipmentId = (Long) timer.getInfo();
        Shipment shipment = em.find(Shipment.class, shipmentId);
        if (shipment == null) {
            return;
        }
        if (shipment.getShipmentStatus() != ShipmentStatus.DELIVERED) {
            shipment.setShipmentStatus(ShipmentStatus.DELAYED);
            auditService.record(
                    "Shipment",
                    shipment.getId(),
                    "DELIVERY_WINDOW_MISSED",
                    "SYSTEM_TIMER",
                    "Shipment " + shipment.getTrackingNumber() + " not marked DELIVERED by expected delivery time " + shipment.getExpectedDeliveryDate()
            );
        }
    }
}
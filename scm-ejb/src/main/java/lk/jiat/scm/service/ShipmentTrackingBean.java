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
import lk.jiat.scm.exception.CarrierSystemUnavailableException;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

@Stateless
public class ShipmentTrackingBean {

    @PersistenceContext(unitName = "SCMPU")
    private EntityManager em;

    @EJB
    private AuditService auditService;

    @Resource
    private TimerService timerService;

    private static final Logger LOGGER = Logger.getLogger(ShipmentTrackingBean.class.getName());

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

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public String checkCarrierStatus(Long shipmentId, boolean simulateTimeout) throws CarrierSystemUnavailableException {
        Shipment shipment = em.find(Shipment.class, shipmentId);
        if (shipment == null) {
            throw new RuntimeException("Shipment not found: " + shipmentId);
        }
        try {
            return queryCarrierSystem(shipment.getTrackingNumber(), simulateTimeout);
        } catch (TimeoutException e) {
            LOGGER.log(Level.WARNING, "Carrier system timeout for shipmentId=" + shipmentId);
            throw new CarrierSystemUnavailableException("Carrier system is currently unavailable, please retry later");
        }
    }

    private String queryCarrierSystem(String trackingNumber, boolean simulateTimeout) throws TimeoutException {
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        if (simulateTimeout) {
            throw new TimeoutException("Simulated carrier system timeout");
        }
        return "IN_TRANSIT";
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
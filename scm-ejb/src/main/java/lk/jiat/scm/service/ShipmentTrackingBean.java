package lk.jiat.scm.service;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import lk.jiat.scm.entity.Shipment;
import lk.jiat.scm.entity.ShipmentStatus;

import java.util.List;

@Stateless
public class ShipmentTrackingBean {

    @PersistenceContext(unitName = "SCMPU")
    private EntityManager em;

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
}
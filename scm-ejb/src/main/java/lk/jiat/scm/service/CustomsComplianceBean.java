package lk.jiat.scm.service;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lk.jiat.scm.entity.CustomsDocument;
import lk.jiat.scm.entity.Shipment;

import java.time.LocalDateTime;
import java.util.List;

@Stateless
public class CustomsComplianceBean {

    @PersistenceContext(unitName = "SCMPU")
    private EntityManager em;

    public CustomsDocument createDocument(Long shipmentId, String documentNumber, String documentType, LocalDateTime complianceDeadline) {
        if (documentNumber == null || documentNumber.isBlank()) {
            throw new RuntimeException("Document number is required");
        }
        if (documentType == null || documentType.isBlank()) {
            throw new RuntimeException("Document type is required");
        }
        if (complianceDeadline == null) {
            throw new RuntimeException("Compliance deadline is required");
        }

        Shipment shipment = em.find(Shipment.class, shipmentId);
        if (shipment == null) {
            throw new RuntimeException("Shipment not found: " + shipmentId);
        }

        CustomsDocument document = new CustomsDocument();
        document.setDocumentNumber(documentNumber);
        document.setDocumentType(documentType);
        document.setComplianceDeadline(complianceDeadline);
        document.setVerified(false);
        document.setShipment(shipment);
        em.persist(document);

        return document;
    }

    public CustomsDocument findById(Long id) {
        return em.find(CustomsDocument.class, id);
    }

    public List<CustomsDocument> findByShipment(Long shipmentId) {
        return em.createQuery("SELECT d FROM CustomsDocument d WHERE d.shipment.id = :shipmentId", CustomsDocument.class)
                .setParameter("shipmentId", shipmentId)
                .getResultList();
    }

    public CustomsDocument verifyDocument(Long documentId) {
        CustomsDocument document = em.find(CustomsDocument.class, documentId);
        if (document == null) {
            throw new RuntimeException("Customs document not found: " + documentId);
        }
        document.setVerified(true);
        return document;
    }

    public List<CustomsDocument> findDocumentsNearingDeadline(int daysAhead) {
        LocalDateTime windowEnd = LocalDateTime.now().plusDays(daysAhead);
        return em.createQuery(
                        "SELECT d FROM CustomsDocument d WHERE d.verified = false AND d.complianceDeadline <= :windowEnd",
                        CustomsDocument.class)
                .setParameter("windowEnd", windowEnd)
                .getResultList();
    }
}
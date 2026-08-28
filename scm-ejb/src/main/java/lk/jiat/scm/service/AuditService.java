package lk.jiat.scm.service;

import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lk.jiat.scm.entity.AuditRecord;

@Stateless
public class AuditService {

    @PersistenceContext(unitName = "SCMPU")
    private EntityManager em;

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public AuditRecord record(String entityType, Long entityId, String action, String performedBy, String details) {
        AuditRecord auditRecord = new AuditRecord();
        auditRecord.setEntityType(entityType);
        auditRecord.setEntityId(entityId);
        auditRecord.setAction(action);
        auditRecord.setPerformedBy(performedBy);
        auditRecord.setDetails(details);
        em.persist(auditRecord);
        return auditRecord;
    }
}
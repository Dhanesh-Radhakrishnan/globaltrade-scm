package lk.jiat.scm.service;

import jakarta.annotation.Resource;
import jakarta.annotation.security.DeclareRoles;
import jakarta.ejb.EJBAccessException;
import jakarta.ejb.SessionContext;
import jakarta.ejb.Stateless;
import jakarta.interceptor.ExcludeClassInterceptors;
import jakarta.interceptor.Interceptors;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lk.jiat.scm.entity.Vendor;
import lk.jiat.scm.interceptor.AuditLoggingInterceptor;
import lk.jiat.scm.interceptor.VendorDataValidationInterceptor;

import java.util.List;

@Stateless
@Interceptors({AuditLoggingInterceptor.class})
@DeclareRoles("VendorRepresentative")
public class VendorServiceBean {

    @Resource
    private SessionContext ctx;

    @PersistenceContext(unitName = "SCMPU")
    private EntityManager em;

    @ExcludeClassInterceptors
    @Interceptors({VendorDataValidationInterceptor.class, AuditLoggingInterceptor.class})
    public Vendor createVendor(Vendor vendor) {
        em.persist(vendor);
        em.flush();
        return vendor;
    }

    @ExcludeClassInterceptors
    public Vendor findById(Long id) {
        return em.find(Vendor.class, id);
    }

    public List<Vendor> findAll() {
        return em.createQuery("SELECT v FROM Vendor v", Vendor.class)
                .getResultList();
    }

    public List<Vendor> findActive() {
        return em.createQuery("SELECT v FROM Vendor v WHERE v.active = true", Vendor.class)
                .getResultList();
    }

    @ExcludeClassInterceptors
    @Interceptors({VendorDataValidationInterceptor.class, AuditLoggingInterceptor.class})
    public Vendor updateVendor(Vendor vendor) {
        Vendor existing = em.find(Vendor.class, vendor.getId());
        if (existing == null) {
            throw new RuntimeException("Vendor not found: " + vendor.getId());
        }
        String caller = ctx.getCallerPrincipal().getName();
        boolean isOwner = caller.equals(existing.getManagedUsername());
        if (!ctx.isCallerInRole("VendorRepresentative") || !isOwner) {
            throw new EJBAccessException("Caller " + caller + " is not authorized to update vendor " + vendor.getId());
        }
        return em.merge(vendor);
    }

    public void deactivateVendor(Long id) {
        Vendor vendor = em.find(Vendor.class, id);
        if (vendor != null) {
            vendor.setActive(false);
        }
    }
}
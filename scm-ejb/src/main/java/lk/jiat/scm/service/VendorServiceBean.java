package lk.jiat.scm.service;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lk.jiat.scm.entity.Vendor;

import java.util.List;

@Stateless
public class VendorServiceBean {

    @PersistenceContext(unitName = "SCMPU")
    private EntityManager em;

    public Vendor createVendor(Vendor vendor) {
        em.persist(vendor);
        return vendor;
    }

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

    public Vendor updateVendor(Vendor vendor) {
        return em.merge(vendor);
    }

    public void deactivateVendor(Long id) {
        Vendor vendor = em.find(Vendor.class, id);
        if (vendor != null) {
            vendor.setActive(false);
        }
    }
}
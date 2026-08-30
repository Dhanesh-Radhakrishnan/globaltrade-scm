package lk.jiat.scm.web;

import jakarta.ejb.EJB;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import lk.jiat.scm.entity.Vendor;
import lk.jiat.scm.service.VendorServiceBean;

import java.util.List;

@Path("/test/vendor")
public class TestVendorResource {

    @EJB
    private VendorServiceBean vendorServiceBean;

    @GET
    @Path("/create")
    public Response createVendor(@QueryParam("vendorName") String vendorName,
                                 @QueryParam("country") String country) {
        Vendor vendor = new Vendor();
        vendor.setVendorName(vendorName);
        vendor.setCountry(country);
        Vendor created = vendorServiceBean.createVendor(vendor);
        return Response.ok(describe(created)).build();
    }

    @GET
    @Path("/update")
    public Response updateVendor(@QueryParam("id") Long id,
                                 @QueryParam("vendorName") String vendorName) {
        Vendor vendor = vendorServiceBean.findById(id);
        if (vendor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("No vendor found for id=" + id)
                    .build();
        }
        vendor.setVendorName(vendorName);
        Vendor updated = vendorServiceBean.updateVendor(vendor);
        return Response.ok(describe(updated)).build();
    }

    @GET
    @Path("/by-id")
    public Response findById(@QueryParam("id") Long id) {
        Vendor vendor = vendorServiceBean.findById(id);
        if (vendor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("No vendor found for id=" + id)
                    .build();
        }
        return Response.ok(describe(vendor)).build();
    }

    @GET
    @Path("/all")
    public Response findAll() {
        List<Vendor> vendors = vendorServiceBean.findAll();
        StringBuilder result = new StringBuilder();
        for (Vendor vendor : vendors) {
            result.append(describe(vendor)).append("\n");
        }
        return Response.ok(result.toString()).build();
    }

    @GET
    @Path("/deactivate")
    public Response deactivateVendor(@QueryParam("id") Long id) {
        vendorServiceBean.deactivateVendor(id);
        return Response.ok("deactivated id=" + id).build();
    }

    @GET
    @Path("/force-system-exception")
    public Response forceSystemException(@QueryParam("vendorId") Long vendorId) {
        vendorServiceBean.forceSystemException(vendorId);
        return Response.ok("should not reach here").build();
    }

    private String describe(Vendor vendor) {
        return "id=" + vendor.getId()
                + ", vendorName=" + vendor.getVendorName()
                + ", country=" + vendor.getCountry()
                + ", active=" + vendor.isActive();
    }
}
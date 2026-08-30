package lk.jiat.scm.web;

import jakarta.ejb.EJB;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import lk.jiat.scm.entity.InventoryItem;
import lk.jiat.scm.exception.InsufficientInventoryException;
import lk.jiat.scm.service.InventoryMonitorBean;

import java.util.List;

@Path("/test/inventory")
public class TestInventoryResource {

    @EJB
    private InventoryMonitorBean inventoryMonitorBean;

    @GET
    @Path("/check")
    public Response checkStock(@QueryParam("inventoryItemId") Long inventoryItemId) {
        InventoryItem item = inventoryMonitorBean.checkStock(inventoryItemId);
        return Response.ok(describe(item)).build();
    }

    @GET
    @Path("/adjust")
    public Response adjustStock(@QueryParam("inventoryItemId") Long inventoryItemId,
                                @QueryParam("delta") int delta) {
        try {
            InventoryItem item = inventoryMonitorBean.adjustStock(inventoryItemId, delta);
            return Response.ok(describe(item)).build();
        } catch (InsufficientInventoryException e) {
            return Response.status(Response.Status.CONFLICT).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/below-threshold")
    public Response findBelowThreshold() {
        List<InventoryItem> items = inventoryMonitorBean.findBelowThreshold();
        StringBuilder result = new StringBuilder();
        for (InventoryItem item : items) {
            result.append(describe(item)).append("\n");
        }
        return Response.ok(result.toString()).build();
    }

    private String describe(InventoryItem item) {
        return "id=" + item.getId()
                + ", sku=" + item.getSku()
                + ", quantityOnHand=" + item.getQuantityOnHand()
                + ", reorderThreshold=" + item.getReorderThreshold()
                + ", unitPrice=" + item.getUnitPrice();
    }
}
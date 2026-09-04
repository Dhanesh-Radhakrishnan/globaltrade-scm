package lk.jiat.scm.rollback;

import jakarta.ejb.EJB;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import lk.jiat.scm.entity.InventoryItem;
import lk.jiat.scm.entity.Order;
import lk.jiat.scm.entity.Vendor;
import lk.jiat.scm.exception.InsufficientInventoryException;
import lk.jiat.scm.service.InventoryMonitorBean;
import lk.jiat.scm.service.OrderProcessingBean;

import java.time.LocalDateTime;

@Path("/rollback")
public class RollbackTestResource {

    @EJB
    private OrderProcessingBean orderProcessingBean;

    @EJB
    private RollbackTestSupportBean supportBean;

    @EJB
    private InventoryMonitorBean inventoryMonitorBean;

    @GET
    @Path("/setup")
    public Response setup(@QueryParam("startingQuantity") int startingQuantity) {
        Vendor vendor = supportBean.createTestVendor();
        InventoryItem item = supportBean.createTestInventoryItem(startingQuantity);
        return Response.ok("vendorId=" + vendor.getId() + ",inventoryItemId=" + item.getId()).build();
    }

    @GET
    @Path("/attempt-order")
    public Response attemptOrder(@QueryParam("vendorId") Long vendorId,
                                 @QueryParam("inventoryItemId") Long inventoryItemId,
                                 @QueryParam("quantity") int quantity) {
        try {
            Order order = orderProcessingBean.placeOrder(vendorId, inventoryItemId, quantity, LocalDateTime.now().plusDays(1));
            return Response.ok("orderId=" + order.getId()).build();
        } catch (InsufficientInventoryException e) {
            return Response.status(Response.Status.CONFLICT).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/attempt-adjust-stock")
    public Response attemptAdjustStock(@QueryParam("inventoryItemId") Long inventoryItemId,
                                       @QueryParam("delta") int delta) throws InsufficientInventoryException {
        InventoryItem item = inventoryMonitorBean.adjustStock(inventoryItemId, delta);
        return Response.ok("quantityOnHand=" + item.getQuantityOnHand()).build();
    }

    @GET
    @Path("/inventory-quantity")
    public Response inventoryQuantity(@QueryParam("inventoryItemId") Long inventoryItemId) {
        return Response.ok(String.valueOf(supportBean.getInventoryQuantity(inventoryItemId))).build();
    }

    @GET
    @Path("/order-count")
    public Response orderCount(@QueryParam("vendorId") Long vendorId) {
        return Response.ok(String.valueOf(supportBean.getOrderCountForVendor(vendorId))).build();
    }

    @GET
    @Path("/cleanup")
    public Response cleanup(@QueryParam("vendorId") Long vendorId,
                            @QueryParam("inventoryItemId") Long inventoryItemId) {
        supportBean.cleanup(vendorId, inventoryItemId);
        return Response.ok("cleaned").build();
    }
}
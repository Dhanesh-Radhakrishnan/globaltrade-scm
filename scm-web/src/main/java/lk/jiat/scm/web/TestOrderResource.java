package lk.jiat.scm.web;

import jakarta.ejb.EJB;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import lk.jiat.scm.entity.Order;
import lk.jiat.scm.service.OrderProcessingBean;

@Path("/test/order")
public class TestOrderResource {

    @EJB
    private OrderProcessingBean orderProcessingBean;

    @GET
    public Response placeTestOrder(@QueryParam("vendorId") Long vendorId,
                                   @QueryParam("inventoryItemId") Long inventoryItemId,
                                   @QueryParam("quantity") int quantity) {
        Order order = orderProcessingBean.placeOrder(vendorId, inventoryItemId, quantity);
        String result = "orderId=" + order.getId()
                + ", orderNumber=" + order.getOrderNumber()
                + ", quantity=" + order.getQuantity()
                + ", totalAmount=" + order.getTotalAmount()
                + ", vendor=" + order.getVendor().getVendorName()
                + ", sku=" + order.getInventoryItem().getSku()
                + ", remainingStock=" + order.getInventoryItem().getQuantityOnHand()
                + ", shipmentTracking=" + order.getShipment().getTrackingNumber()
                + ", shipmentStatus=" + order.getShipment().getShipmentStatus();
        return Response.ok(result).build();
    }
}
package lk.jiat.scm.web;

import jakarta.ejb.EJB;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import lk.jiat.scm.entity.Order;
import lk.jiat.scm.service.OrderProcessingBean;

import java.time.LocalDateTime;

@Path("/test/order")
public class TestOrderResource {

    @EJB
    private OrderProcessingBean orderProcessingBean;

    @GET
    public Response placeTestOrder(@QueryParam("vendorId") Long vendorId,
                                   @QueryParam("inventoryItemId") Long inventoryItemId,
                                   @QueryParam("quantity") int quantity,
                                   @QueryParam("deliveryInSeconds") int deliveryInSeconds) {
        LocalDateTime expectedDeliveryDate = LocalDateTime.now().plusSeconds(deliveryInSeconds);
        Order order = orderProcessingBean.placeOrder(vendorId, inventoryItemId, quantity, expectedDeliveryDate);
        String result = "orderId=" + order.getId()
                + ", orderNumber=" + order.getOrderNumber()
                + ", quantity=" + order.getQuantity()
                + ", totalAmount=" + order.getTotalAmount()
                + ", shipmentId=" + order.getShipment().getId()
                + ", expectedDeliveryDate=" + order.getShipment().getExpectedDeliveryDate();
        return Response.ok(result).build();
    }
}
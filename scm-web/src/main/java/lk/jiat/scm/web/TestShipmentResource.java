package lk.jiat.scm.web;

import jakarta.ejb.EJB;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import lk.jiat.scm.entity.Shipment;
import lk.jiat.scm.entity.ShipmentStatus;
import lk.jiat.scm.exception.CarrierSystemUnavailableException;
import lk.jiat.scm.service.ShipmentTrackingBean;

import java.time.LocalDateTime;

@Path("/test/shipment")
public class TestShipmentResource {

    @EJB
    private ShipmentTrackingBean shipmentTrackingBean;

    @GET
    @Path("/update-status")
    public Response updateStatus(@QueryParam("shipmentId") Long shipmentId,
                                 @QueryParam("status") String status) {
        ShipmentStatus newStatus = ShipmentStatus.valueOf(status);
        Shipment shipment = shipmentTrackingBean.updateStatus(shipmentId, newStatus);
        return Response.ok(describe(shipment)).build();
    }

    @GET
    @Path("/by-tracking-number")
    public Response findByTrackingNumber(@QueryParam("trackingNumber") String trackingNumber) {
        Shipment shipment = shipmentTrackingBean.findByTrackingNumber(trackingNumber);
        if (shipment == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("No shipment found for trackingNumber=" + trackingNumber)
                    .build();
        }
        return Response.ok(describe(shipment)).build();
    }

    @GET
    @Path("/by-status")
    public Response findByStatus(@QueryParam("status") String status) {
        ShipmentStatus statusEnum = ShipmentStatus.valueOf(status);
        StringBuilder result = new StringBuilder();
        for (Shipment shipment : shipmentTrackingBean.findByStatus(statusEnum)) {
            result.append(describe(shipment)).append("\n");
        }
        return Response.ok(result.toString()).build();
    }

    @GET
    @Path("/schedule-delivery-check")
    public Response scheduleDeliveryCheck(@QueryParam("shipmentId") Long shipmentId,
                                          @QueryParam("secondsFromNow") int secondsFromNow) {
        LocalDateTime expectedDeliveryDate = LocalDateTime.now().plusSeconds(secondsFromNow);
        Shipment shipment = shipmentTrackingBean.scheduleDeliveryCheck(shipmentId, expectedDeliveryDate);
        return Response.ok(describe(shipment)).build();
    }

    @GET
    @Path("/carrier-status")
    public Response checkCarrierStatus(@QueryParam("shipmentId") Long shipmentId,
                                       @QueryParam("simulateTimeout") boolean simulateTimeout) {
        try {
            String status = shipmentTrackingBean.checkCarrierStatus(shipmentId, simulateTimeout);
            return Response.ok("carrierStatus=" + status).build();
        } catch (CarrierSystemUnavailableException e) {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(e.getMessage()).build();
        }
    }

    private String describe(Shipment shipment) {
        return "shipmentId=" + shipment.getId()
                + ", trackingNumber=" + shipment.getTrackingNumber()
                + ", status=" + shipment.getShipmentStatus()
                + ", expectedDeliveryDate=" + shipment.getExpectedDeliveryDate();
    }
}
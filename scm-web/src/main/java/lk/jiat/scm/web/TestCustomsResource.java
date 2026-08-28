package lk.jiat.scm.web;

import jakarta.ejb.EJB;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import lk.jiat.scm.entity.CustomsDocument;
import lk.jiat.scm.service.CustomsComplianceBean;

import java.time.LocalDateTime;
import java.util.List;

@Path("/test/customs")
public class TestCustomsResource {

    @EJB
    private CustomsComplianceBean customsComplianceBean;

    @GET
    @Path("/create")
    public Response createDocument(@QueryParam("shipmentId") Long shipmentId,
                                   @QueryParam("documentNumber") String documentNumber,
                                   @QueryParam("documentType") String documentType,
                                   @QueryParam("daysUntilDeadline") int daysUntilDeadline) {
        LocalDateTime deadline = LocalDateTime.now().plusDays(daysUntilDeadline);
        CustomsDocument document = customsComplianceBean.createDocument(shipmentId, documentNumber, documentType, deadline);
        return Response.ok(describe(document)).build();
    }

    @GET
    @Path("/verify")
    public Response verifyDocument(@QueryParam("documentId") Long documentId) {
        CustomsDocument document = customsComplianceBean.verifyDocument(documentId);
        return Response.ok(describe(document)).build();
    }

    @GET
    @Path("/by-shipment")
    public Response findByShipment(@QueryParam("shipmentId") Long shipmentId) {
        StringBuilder result = new StringBuilder();
        for (CustomsDocument document : customsComplianceBean.findByShipment(shipmentId)) {
            result.append(describe(document)).append("\n");
        }
        return Response.ok(result.toString()).build();
    }

    @GET
    @Path("/nearing-deadline")
    public Response findNearingDeadline(@QueryParam("daysAhead") int daysAhead) {
        List<CustomsDocument> documents = customsComplianceBean.findDocumentsNearingDeadline(daysAhead);
        StringBuilder result = new StringBuilder();
        for (CustomsDocument document : documents) {
            result.append(describe(document)).append("\n");
        }
        return Response.ok(result.toString()).build();
    }

    private String describe(CustomsDocument document) {
        return "documentId=" + document.getId()
                + ", documentNumber=" + document.getDocumentNumber()
                + ", documentType=" + document.getDocumentType()
                + ", complianceDeadline=" + document.getComplianceDeadline()
                + ", verified=" + document.isVerified()
                + ", shipmentId=" + document.getShipment().getId();
    }
}
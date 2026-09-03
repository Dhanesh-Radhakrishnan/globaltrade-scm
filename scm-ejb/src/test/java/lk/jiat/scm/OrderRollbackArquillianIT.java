package lk.jiat.scm;

import jakarta.ws.rs.core.Response;
import lk.jiat.scm.entity.AuditRecord;
import lk.jiat.scm.entity.CustomsDocument;
import lk.jiat.scm.entity.InventoryItem;
import lk.jiat.scm.entity.OrderStatus;
import lk.jiat.scm.entity.Shipment;
import lk.jiat.scm.entity.ShipmentStatus;
import lk.jiat.scm.entity.Vendor;
import lk.jiat.scm.exception.InsufficientInventoryException;
import lk.jiat.scm.interceptor.AuditLoggingInterceptor;
import lk.jiat.scm.interceptor.VendorDataValidationInterceptor;
import lk.jiat.scm.rollback.RollbackTestApplication;
import lk.jiat.scm.rollback.RollbackTestResource;
import lk.jiat.scm.rollback.RollbackTestSupportBean;
import lk.jiat.scm.service.AuditService;
import lk.jiat.scm.service.InventoryMonitorBean;
import lk.jiat.scm.service.OrderProcessingBean;
import lk.jiat.scm.service.ShipmentTrackingBean;
import lk.jiat.scm.service.VendorServiceBean;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(ArquillianExtension.class)
class OrderRollbackArquillianIT {

    private static final String WEB_XML = """
            <?xml version="1.0" encoding="UTF-8"?>
            <web-app xmlns="https://jakarta.ee/xml/ns/jakartaee"
                     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                     xsi:schemaLocation="https://jakarta.ee/xml/ns/jakartaee https://jakarta.ee/xml/ns/jakartaee/web-app_6_0.xsd"
                     version="6.0">
                <security-constraint>
                    <web-resource-collection>
                        <web-resource-name>RollbackTest</web-resource-name>
                        <url-pattern>/api/rollback/*</url-pattern>
                    </web-resource-collection>
                    <auth-constraint>
                        <role-name>LogisticsCoordinator</role-name>
                    </auth-constraint>
                </security-constraint>
                <login-config>
                    <auth-method>BASIC</auth-method>
                    <realm-name>SCMRealm</realm-name>
                </login-config>
                <security-role>
                    <role-name>LogisticsCoordinator</role-name>
                </security-role>
            </web-app>
            """;

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class, "order-rollback-test.war")
                .addClasses(
                        Vendor.class,
                        lk.jiat.scm.entity.Order.class,
                        OrderStatus.class,
                        Shipment.class,
                        ShipmentStatus.class,
                        InventoryItem.class,
                        CustomsDocument.class,
                        AuditRecord.class
                )
                .addClasses(
                        OrderProcessingBean.class,
                        VendorServiceBean.class,
                        InventoryMonitorBean.class,
                        ShipmentTrackingBean.class,
                        AuditService.class
                )
                .addClasses(
                        AuditLoggingInterceptor.class,
                        VendorDataValidationInterceptor.class
                )
                .addClass(InsufficientInventoryException.class)
                .addClasses(
                        RollbackTestApplication.class,
                        RollbackTestResource.class,
                        RollbackTestSupportBean.class
                )
                .addAsResource("META-INF/persistence.xml", "META-INF/persistence.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsWebInfResource(new StringAsset(WEB_XML), "web.xml");
    }

    private static final String COORDINATOR_USERNAME = "coordinator1";

    private String basicAuthHeader() {
        String password = System.getenv("SCM_TEST_COORDINATOR_PASSWORD");
        if (password == null || password.isBlank()) {
            throw new IllegalStateException("SCM_TEST_COORDINATOR_PASSWORD environment variable is not set");
        }
        String credentials = COORDINATOR_USERNAME + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());
    }

    @Test
    @RunAsClient
    void insufficientStockLeavesNoOrderAndInventoryUnchanged(@ArquillianResource URL baseUrl) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        String authHeader = basicAuthHeader();

        HttpRequest setupRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "api/rollback/setup?startingQuantity=5"))
                .header("Authorization", authHeader)
                .GET()
                .build();
        HttpResponse<String> setupResponse = client.send(setupRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, setupResponse.statusCode());

        String[] parts = setupResponse.body().split(",");
        Long vendorId = Long.valueOf(parts[0].split("=")[1]);
        Long inventoryItemId = Long.valueOf(parts[1].split("=")[1]);

        try {
            HttpRequest attemptRequest = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "api/rollback/attempt-order?vendorId=" + vendorId
                            + "&inventoryItemId=" + inventoryItemId + "&quantity=999"))
                    .header("Authorization", authHeader)
                    .GET()
                    .build();
            HttpResponse<String> attemptResponse = client.send(attemptRequest, HttpResponse.BodyHandlers.ofString());
            assertEquals(409, attemptResponse.statusCode());

            HttpRequest quantityRequest = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "api/rollback/inventory-quantity?inventoryItemId=" + inventoryItemId))
                    .header("Authorization", authHeader)
                    .GET()
                    .build();
            HttpResponse<String> quantityResponse = client.send(quantityRequest, HttpResponse.BodyHandlers.ofString());
            assertEquals("5", quantityResponse.body());

            HttpRequest orderCountRequest = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "api/rollback/order-count?vendorId=" + vendorId))
                    .header("Authorization", authHeader)
                    .GET()
                    .build();
            HttpResponse<String> orderCountResponse = client.send(orderCountRequest, HttpResponse.BodyHandlers.ofString());
            assertEquals("0", orderCountResponse.body());
        } finally {
            HttpRequest cleanupRequest = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "api/rollback/cleanup?vendorId=" + vendorId + "&inventoryItemId=" + inventoryItemId))
                    .header("Authorization", authHeader)
                    .GET()
                    .build();
            client.send(cleanupRequest, HttpResponse.BodyHandlers.ofString());
        }
    }
}
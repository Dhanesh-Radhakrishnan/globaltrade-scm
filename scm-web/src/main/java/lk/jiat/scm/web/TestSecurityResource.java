package lk.jiat.scm.web;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

@Path("/test/security")
public class TestSecurityResource {

    @GET
    @Path("/whoami")
    public Response whoAmI(@Context SecurityContext securityContext) {
        String username = securityContext.getUserPrincipal() != null
                ? securityContext.getUserPrincipal().getName()
                : "anonymous";
        boolean isCoordinator = securityContext.isUserInRole("LogisticsCoordinator");
        String result = "username=" + username + ", isLogisticsCoordinator=" + isCoordinator;
        return Response.ok(result).build();
    }
}
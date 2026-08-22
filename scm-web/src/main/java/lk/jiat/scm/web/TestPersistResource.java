package lk.jiat.scm.web;

import jakarta.ejb.EJB;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

import lk.jiat.scm.test.TestPersistenceBean;

@Path("/test/persist")
public class TestPersistResource {

    @EJB
    private TestPersistenceBean testPersistenceBean;

    @GET
    public Response persist() {
        String result = testPersistenceBean.runThrowawayInsert();
        return Response.ok(result).build();
    }
}
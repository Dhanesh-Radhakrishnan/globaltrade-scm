package lk.jiat.scm.interceptor;

import jakarta.ejb.EJB;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.InvocationContext;
import lk.jiat.scm.entity.Order;
import lk.jiat.scm.entity.Vendor;
import lk.jiat.scm.service.AuditService;

public class AuditLoggingInterceptor {

    @EJB
    private AuditService auditService;

    @AroundInvoke
    public Object logInvocation(InvocationContext ic) throws Exception {
        long startTime = System.currentTimeMillis();
        Object result = ic.proceed();
        long elapsed = System.currentTimeMillis() - startTime;

        String entityType = ic.getTarget().getClass().getSimpleName();
        Long entityId = 0L;

        if (result instanceof Vendor vendor) {
            entityType = "Vendor";
            entityId = vendor.getId() != null ? vendor.getId() : 0L;
        } else if (result instanceof Order order) {
            entityType = "Order";
            entityId = order.getId() != null ? order.getId() : 0L;
        }

        auditService.record(
                entityType,
                entityId,
                ic.getMethod().getName(),
                "SYSTEM",
                "Executed in " + elapsed + "ms"
        );

        return result;
    }
}
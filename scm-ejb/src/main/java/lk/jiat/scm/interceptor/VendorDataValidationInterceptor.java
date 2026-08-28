package lk.jiat.scm.interceptor;

import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.InvocationContext;
import lk.jiat.scm.entity.Vendor;

public class VendorDataValidationInterceptor {

    @AroundInvoke
    public Object validate(InvocationContext ic) throws Exception {
        for (Object arg : ic.getParameters()) {
            if (arg instanceof Vendor vendor) {
                if (vendor.getVendorName() == null || vendor.getVendorName().isBlank()) {
                    throw new RuntimeException("Vendor name is required");
                }
            }
        }
        return ic.proceed();
    }
}
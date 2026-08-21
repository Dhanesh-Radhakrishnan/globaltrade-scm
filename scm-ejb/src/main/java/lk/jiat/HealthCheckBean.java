package lk.jiat;

import jakarta.ejb.Stateless;

@Stateless
public class HealthCheckBean {
    public String ping() {
        return "EJB is alive!";
    }
}

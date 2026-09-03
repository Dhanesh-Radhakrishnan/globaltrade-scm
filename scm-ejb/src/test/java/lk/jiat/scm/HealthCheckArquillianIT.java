package lk.jiat.scm;

import jakarta.ejb.EJB;
import lk.jiat.HealthCheckBean;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(ArquillianExtension.class)
class HealthCheckArquillianIT {

    @Deployment
    public static JavaArchive createDeployment() {
        return ShrinkWrap.create(JavaArchive.class, "healthcheck-smoke-test.jar")
                .addClass(HealthCheckBean.class);
    }

    @EJB
    private HealthCheckBean healthCheckBean;

    @Test
    void ejbIsInjectedAndRespondsInsideRemoteContainer() {
        assertEquals("EJB is alive!", healthCheckBean.ping());
    }
}
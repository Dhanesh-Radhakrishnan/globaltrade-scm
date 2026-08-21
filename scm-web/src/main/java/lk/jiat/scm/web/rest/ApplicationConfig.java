package lk.jiat.scm.web.rest;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

@ApplicationPath("/")
public class ApplicationConfig extends Application {
    // Intentionally empty – this is all you need to enable JAX-RS scanning.
}
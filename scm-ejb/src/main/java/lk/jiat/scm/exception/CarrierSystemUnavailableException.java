package lk.jiat.scm.exception;

import jakarta.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class CarrierSystemUnavailableException extends Exception {

    public CarrierSystemUnavailableException(String message) {
        super(message);
    }
}
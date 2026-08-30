package lk.jiat.scm.exception;

import jakarta.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class InsufficientInventoryException extends Exception {

    public InsufficientInventoryException(String message) {
        super(message);
    }
}
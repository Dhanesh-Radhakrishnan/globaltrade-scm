package lk.jiat.scm.exception;

import jakarta.ejb.ApplicationException;

@ApplicationException(rollback = false)
public class CustomsDocumentInvalidException extends Exception {

    public CustomsDocumentInvalidException(String message) {
        super(message);
    }
}
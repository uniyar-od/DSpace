/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.exception;
import java.util.List;

import org.dspace.validation.model.ValidationError;
import org.springframework.http.HttpStatus;

/**
 * @author Mykhaylo Boychuk (mykhaylo.boychuk at 4science.it)
 */
public class UnprocessableEditExceptionCustomError {

    private String message;
    private HttpStatus status;
    private List<ValidationError> errors;

    public UnprocessableEditExceptionCustomError(HttpStatus unprocessableEntity, String message,
            List<ValidationError> errors) {
        this.status = unprocessableEntity;
        this.message = message;
        setErrors(errors);
    }

    public UnprocessableEditExceptionCustomError(List<ValidationError> errors) {
        setErrors(errors);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public void setStatus(HttpStatus status) {
        this.status = status;
    }

    public List<ValidationError> getErrors() {
        return errors;
    }

    public void setErrors(List<ValidationError> errors) {
        this.errors = errors;
    }

}
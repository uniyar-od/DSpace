/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.layout;

/**
 * invalid rendering exception
 *
 * @author Mohamed Eskander (mohamed.eskander at 4science.it)
 */
public class InvalidRenderingException extends Exception {

    public InvalidRenderingException(String message) {
        super(message);
    }

    public InvalidRenderingException(String message, Exception cause) {
        super(message, cause);
    }
}

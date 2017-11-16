/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

/**
 *
 * @author nicolo.boschi
 */
public class BadRequestException extends Exception {

    private static final long serialVersionUID = 1L;

    public BadRequestException(Throwable cause) {
        super(cause);
    }

    public BadRequestException(String cause) {
        super(cause);
    }
}

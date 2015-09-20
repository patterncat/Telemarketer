package edu.telemarketer.http.exceptions;

/**
 * Be careful!
 * Created by hason on 15/9/20.
 */
public class ServerInternalException extends RuntimeException {
    public ServerInternalException(String msg) {
        super(msg);
    }
}

package edu.telemarketer.http.exceptions;

/**
 * Be careful!
 * Created by hason on 15/9/20.
 */
public class IllegalRequestException extends RuntimeException {
    public IllegalRequestException(String msg) {
        super(msg);
    }

    public IllegalRequestException(String msg,Throwable e) {
        super(msg,e);
    }
}

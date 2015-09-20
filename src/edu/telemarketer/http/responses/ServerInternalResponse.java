package edu.telemarketer.http.responses;

import edu.telemarketer.http.Status;

/**
 * Be careful!
 * Created by hason on 15/9/20.
 */
public class ServerInternalResponse extends Response {

    public ServerInternalResponse() {
        super(Status.INTERNAL_SERVER_ERROR_500);

    }
}

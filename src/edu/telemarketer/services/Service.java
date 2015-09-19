package edu.telemarketer.services;

import edu.telemarketer.http.requests.Request;
import edu.telemarketer.http.responses.Response;

/**
 * Be careful!
 * Created by hason on 15/9/18.
 */
public interface Service {

    Response execute(Request request);
}
